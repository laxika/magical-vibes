#!/usr/bin/env python3
"""Step 15, run 17z11f2: docs (idempotent; fails if a doc still names a removed form outside a "Gone" line)."""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"


def edit(name, replacements):
    path = DOCS / name
    text = path.read_text()
    for old, new in replacements:
        if old not in text:
            if new not in text:
                sys.exit(f"{name}: anchor not found: {old[:80]!r}")
            continue
        text = text.replace(old, new)
    path.write_text(text)


edit("EFFECTS_INDEX.md", [
    ("| `ExileTargetOnControllerSpellCastEffect` | `()` | Marker effect stored in Emblem.staticEffects. Triggers when "
     "controller casts a spell, prompting target permanent choice then exiling it |",
     "| `ExileTargetOnControllerSpellCastEffect` | — | **Gone** (effect-slot-refactor.md step 15, run 17z11f2): Venser, "
     "the Sojourner's emblem is `CreateEmblemEffect(new TriggeredAbility(Triggers.youCastSpell(), new "
     "ExileTargetPermanentEffect()), …)`, read by the emblem cast reader |"),
])

CASCADE_OLD = ("queued as a plain triggered ability via the generic self-cast branch, then `CascadeEffectHandler` digs "
               "the library until a nonland card with mana value **strictly less** than the cascade spell's "
               "(`entry.getCard().getManaValue()`)")
CASCADE_NEW = ("each `CascadeEffect` its own `addTrigger` (CR 702.85c: each instance triggers separately; the self-cast "
               "collector puts each ability on the stack as one object, recording the spell as its triggering spell), "
               "then `CascadeEffectHandler` digs the library until a nonland card with mana value **strictly less** "
               "than the spell's (read as the ability resolves, as it last existed on the stack once it has left)")
edit("EFFECTS_QUICK_REFERENCE.md", [
    (CASCADE_OLD, CASCADE_NEW),
    (CASCADE_OLD, CASCADE_NEW),
    ("- `CascadeEffect()` in a static `GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant.CASCADE_TO_SLIVER_SPELL, …)`",
     "- \"Spells you cast [quality] have cascade\" (Imoti, Zhulodok's \"cascade, cascade\", TARDIS Bay, Wild-Magic "
     "Sorcerer): `addTrigger(Triggers.youCastSpell().withFilter(…).grantedToTheSpell(), new CascadeEffect()[, new "
     "CascadeEffect()])` — `grantedToTheSpell()` makes the ability the spell's (CR 113.7): each effect goes on the stack "
     "as its own ability sourced by the spell, and nothing that makes the permanent's abilities trigger additional "
     "times applies (Roaming Throne). A permanent's own \"whenever you cast …, exile cards until …\" (Jodah, the "
     "Unifier) is written without it.\n"
     "- `CascadeEffect()` in a static `GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant.CASCADE_TO_SLIVER_SPELL, …)`"),
])
edit("ARCHITECTURE.md", [
    ("The \"when you cast this spell\" reader still puts the plain effects of all the card's such abilities on the "
     "stack as one object, as cards wrote one ability as several.",
     "The \"when you cast this spell\" reader (`collectSelfCastAbilities`) puts each such ability on the stack as one "
     "object (CR 603.3), a keyword's (cascade, demonstrate, ripple, replicate, storm) as any other, and records the "
     "spell (its X, kick, cast zone and the spell itself) as the entry's triggering spell whatever the effects are; a "
     "\"you may\" is asked as it resolves (CR 603.5). A cast trigger with `.grantedToTheSpell()` (\"spells you cast … "
     "have cascade\") is the spell's ability: each effect its own object sourced by the spell, never triggered "
     "additional times."),
])

for doc in DOCS.glob("*.md"):
    for line in doc.read_text().split("\n"):
        for removed in ("ExileTargetOnControllerSpellCastEffect", "TriggeringSpellReferencingEffect",
                        "forSpellCastTrigger", "DemonstrateEffect.isWrappedBy"):
            if removed in line and "**Gone**" not in line:
                sys.exit(f"{doc.name} still names {removed}: {line[:120]}")
print("docs edited")
