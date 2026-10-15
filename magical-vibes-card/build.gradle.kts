// Umbrella over the card submodules: consumers depend on this project alone and see every card.
// Cards are split by starting letter so the letter modules compile in parallel; cards that are
// constructed by a card of another letter (back faces, meld partners, conjure targets) live in
// :shared, which every letter module depends on, because letter modules can't depend on each other.
dependencies {
    api(project(":magical-vibes-card:core"))
    api(project(":magical-vibes-card:shared"))
    ('a'..'z').forEach { "api"(project(":magical-vibes-card:$it")) }
}

subprojects {
    if (name.length == 1) {
        dependencies {
            "api"(project(":magical-vibes-card:core"))
            "implementation"(project(":magical-vibes-card:shared"))
        }
    }
}
