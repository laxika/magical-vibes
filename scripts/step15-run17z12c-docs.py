#!/usr/bin/env python3
"""Step 15, run 17z12c: agent-docs follow the damage and death cards' conversion.

`EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect` is gone (Spiteful Shadows, Binding Agony, Backfire,
Repercussion read the trigger event with `DealDamageToPlayersEffect`), `DealDamageToAnyTargetEffect.fromEnteringPermanent`
is `dealtByTriggeringPermanent` (also Fiendlash, Wrathful Raptors, Wrathful Red Dragon), and Bounty Board's bounty
counter is the trigger's filter. Each line is replaced only when it still reads as before.
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
TO_CONTROLLER = "`DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)`"
GONE = ("**Gone** (effect-slot-refactor.md step 15, run 17z12c): \"this Aura deals that much damage to that creature's "
        "controller\" (Binding Agony, Backfire, Repercussion) is " + TO_CONTROLLER + "; Spiteful Shadows' \"it deals that "
        "much damage to its controller\" the same `.dealtByTriggeringPermanent()`. The collectors record the creature as "
        "the triggering permanent and the damage as the event value")


def edit(name, pairs):
    path = DOCS / name
    text = path.read_text(encoding="utf-8", errors="surrogateescape")
    for old_start, new in pairs:
        lines = text.split("\n")
        hits = [i for i, line in enumerate(lines) if line.startswith(old_start)]
        assert hits, (name, old_start)
        for i in hits:
            lines[i] = new
        text = "\n".join(lines)
    path.write_text(text, encoding="utf-8", errors="surrogateescape")
    print("edited", name)


edit("EFFECTS_INDEX.md", [
    ("| `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect` |",
     "| `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect` | — | " + GONE + " |"),
    ("| `EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect` |",
     "| `EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect` | `(int lifeGain)` | each opponent of the "
     "triggering (dying) creature's last-known controller draws a card and gains life; the bounty counter is the "
     "trigger's filter (`Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.BOUNTY), "
     "ObjectRelation.ANY)`). | Bounty Board (OTC 37) |"),
])
edit("EFFECTS_QUICK_REFERENCE.md", [
    ("- `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect()`",
     "- `EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect` — " + GONE),
    ("- `DealDamageToAnyTargetEffect.fromEnteringPermanent(DynamicAmount)`",
     "- `DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(DynamicAmount)`: the stack entry's triggering permanent "
     "(the creature that entered, was dealt damage, …) deals the damage, live or as it last existed (CR 608.2h): "
     "\"it deals damage equal to its power\" (Terror of the Peaks, Fiendlash), \"it deals that much damage\" (Wrathful "
     "Raptors, with `EventValue`). Combine with `MayEffect(..., null, MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)` "
     "for Pandemonium: the entering creature's controller chooses the target before resolution and makes the optional "
     "choice during resolution, while the enchantment's controller controls the trigger. Targeted enter-trigger may "
     "effects also support that chooser without changing the damage source (Death Match)."),
    ("- `EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect(int)`",
     "- `EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect(int)` - each opponent of the triggering (dying) "
     "creature's last-known controller draws a card and gains the configured life; \"a creature with a bounty counter "
     "on it\" is the trigger's filter (Bounty Board, OTC 37)"),
])
edit("ORACLE_TEXT_EFFECT_MAP.md", [
    ("| \"whenever enchanted creature is dealt damage, it deals that much damage to its controller\" |",
     "| \"whenever enchanted creature is dealt damage, it deals that much damage to its controller\" | "
     + TO_CONTROLLER[:-1] + ".dealtByTriggeringPermanent()` | `Triggers.enchantedCreatureDealtDamage()` | Spiteful "
     "Shadows |"),
    ("| \"whenever enchanted creature deals damage to you, this Aura deals that much damage to that creature's controller\" |",
     "| \"whenever enchanted creature deals damage to you, this Aura deals that much damage to that creature's "
     "controller\" | " + TO_CONTROLLER + " | `Triggers.enchantedCreatureDealsDamageToYou()` | Backfire |"),
    ("| \"whenever enchanted creature is dealt damage, this Aura deals that much damage to that creature's controller\" |",
     "| \"whenever enchanted creature is dealt damage, this Aura deals that much damage to that creature's controller\" "
     "| " + TO_CONTROLLER + " | `Triggers.enchantedCreatureDealtDamage()` | Binding Agony (Repercussion on "
     "`Triggers.creatureDealtDamage()`) |"),
])
edit("ACTIVATED_ABILITY_GUIDE.md", [
    ("| `Triggers.enchantedCreatureDealtDamage()` |",
     "| `Triggers.enchantedCreatureDealtDamage()` | The creature this aura or equipment is attached to is dealt damage "
     "(combat or non-combat). The ability is the Aura's or Equipment's; its entry records the creature as the "
     "triggering permanent (id, controller, power, snapshot) and the damage as the event value, whatever the effects "
     "are; a targeted ability's target is chosen as it goes on the stack. Effects: " + TO_CONTROLLER
     + " (Binding Agony; `.dealtByTriggeringPermanent()` for Spiteful Shadows), "
     "`DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower())` (Fiendlash), "
     "`DestroyReferencedPermanentEffect(TRIGGERING)` (Mortal Wound: the creature dealt damage), "
     "`LoseLifeEffect(new EventValue(), TRIGGERING_PERMANENT_CONTROLLER)` (Ragged Veins — life loss equal to the "
     "damage) |"),
])
edit("CARD_PATTERNS_PERMANENTS_STATIC.md", [
    ("| Enchanted creature dealt damage trigger | `s/SpitefulShadows.java` |",
     "| Enchanted creature dealt damage trigger | `s/SpitefulShadows.java` | Triggers.enchantedCreatureDealtDamage() "
     + TO_CONTROLLER + ".dealtByTriggeringPermanent() — enchanted creature deals damage equal to the amount dealt to "
     "it to its controller |"),
    ("| Enchanted creature dealt damage → Aura burns its controller | `b/BindingAgony.java` |",
     "| Enchanted creature dealt damage → Aura burns its controller | `b/BindingAgony.java` | target creature + "
     "Triggers.enchantedCreatureDealtDamage() " + TO_CONTROLLER + " — the Aura deals the damage |"),
    ("| Enchanted creature deals damage to you → reflect to its controller | `b/Backfire.java` |",
     "| Enchanted creature deals damage to you → reflect to its controller | `b/Backfire.java` | target creature + "
     "Triggers.enchantedCreatureDealsDamageToYou() " + TO_CONTROLLER + " — because the Aura is on its controller's "
     "battlefield the trigger only fires when the damaged player is the Aura's controller (combat + noncombat) |"),
    ("| Enchanted creature dealt damage \u00e2\u2020\u2019 Aura burns its controller | `b/BindingAgony.java` |",
     "| Enchanted creature dealt damage \u00e2\u2020\u2019 Aura burns its controller | `b/BindingAgony.java` | "
     "target creature + Triggers.enchantedCreatureDealtDamage() " + TO_CONTROLLER + " \u00e2\u20ac\u201d the Aura "
     "deals the damage |"),
    ("| Enchanted creature deals damage to you \u00e2\u2020\u2019 reflect to its controller | `b/Backfire.java` |",
     "| Enchanted creature deals damage to you \u00e2\u2020\u2019 reflect to its controller | `b/Backfire.java` | "
     "target creature + Triggers.enchantedCreatureDealsDamageToYou() " + TO_CONTROLLER + " \u00e2\u20ac\u201d "
     "because the Aura is on its controller's battlefield the trigger only fires when the damaged player is the "
     "Aura's controller (combat + noncombat) |"),
])
