plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "magical-vibes"

include("magical-vibes-frontend")
include("magical-vibes-application")
include("magical-vibes-webservice")
include("magical-vibes-ai")
include("magical-vibes-domain")
include("magical-vibes-card")
include("magical-vibes-card:core")
include("magical-vibes-card:shared")
('a'..'z').forEach { include("magical-vibes-card:$it") }
include("magical-vibes-card-test")
('a'..'z').forEach { include("magical-vibes-card-test:$it") }
include("magical-vibes-engine")
include("magical-vibes-card-data")
include("magical-vibes-websocket")
include("magical-vibes-networking")
include("magical-vibes-review-server")
