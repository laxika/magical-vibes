#!/usr/bin/env python3
"""Step 15, run 17z14c2: the cards whose abilities `TriggerCollectionService`'s damage and becomes-the-target
collectors read differently now that each ability goes on the stack whole (row 17z14c2; row 17z14h's list,
`step15-run17z14-scan.py`, and `step15-run17z14c2-cards-scan.py`).

The collectors put each triggered ability on the stack as one object of its effects (CR 603.3), no longer one object of
every ability of a watch, so an ability split into several `addTrigger` calls is several objects. Each card under
`EDITS` was read against its Oracle text (Scryfall MCP) and prints one ability there; its calls become one
`addTrigger`, its effects in the order the merging collector put them on the stack.

Leyline of Combustion's "whenever you and/or at least one permanent you control becomes the target of a spell or
ability an opponent controls" triggers once for the spell or ability (its ruling), where "you or a permanent you
control becomes the target" triggers once for each such target (Rayne's, Leovold's and Unsettled Mariner's rulings,
CR 603.2c). The collector told them apart by the class of the ability's effect (a card draw); the Leyline now says it
with `TriggerOptions.ONE_OR_MORE`.

Usage: step15-run17z14c2-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

# card -> [(old, new)]
EDITS = {
    # "When this creature becomes the target of a spell, sacrifice it and create a number of 1/1 colorless Construct
    # artifact creature tokens equal to its power." One ability; the collector joined the two calls into one object.
    "MyriadConstruct": [(
        """        addTrigger(Triggers.becomesTargetOfSpell(), new SacrificeSelfEffect());
        addTrigger(Triggers.becomesTargetOfSpell(), new CreateTokenEffect(""",
        """        addTrigger(Triggers.becomesTargetOfSpell(), new SacrificeSelfEffect(), new CreateTokenEffect(""")],
    # "Whenever a creature deals combat damage to you, destroy that creature. Create a 1/1 white and black Spirit
    # creature token with flying." One ability; the collector joined the two calls into one object.
    "TeysaEnvoyOfGhosts": [(
        """        addTrigger(Triggers.creatureDealsCombatDamageToYou(), new DestroyTargetPermanentEffect());
        addTrigger(Triggers.creatureDealsCombatDamageToYou(), new CreateTokenEffect(""",
        """        addTrigger(Triggers.creatureDealsCombatDamageToYou(), new DestroyTargetPermanentEffect(),
                new CreateTokenEffect(""")],
    # "Whenever you or a permanent you control becomes the target of a spell or ability an opponent controls, you may
    # draw a card. You may draw an additional card if Rayne is enchanted." One ability; the collector joined the two
    # calls into one object.
    "RayneAcademyChancellor": [(
        """                new MayEffect(new DrawCardEffect(), "Draw a card?"));
        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new ConditionalEffect(new Enchanted(),""",
        """                new MayEffect(new DrawCardEffect(), "Draw a card?"),
                new ConditionalEffect(new Enchanted(),""")],
    # "Whenever you and/or at least one permanent you control becomes the target of a spell or ability an opponent
    # controls, this enchantment deals 2 damage to that player." Once for the spell or ability (its ruling).
    "LeylineOfCombustion": [(
        """        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));""",
        """        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                TriggerOptions.ONE_OR_MORE,
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));"""), (
        """import com.github.laxika.magicalvibes.model.trigger.Triggers;""",
        """import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""")],
}


def find(name):
    for path in ROOT.glob(f"*/{PKG}/*/{name}.java"):
        return path.relative_to(ROOT).parts[0], path
    raise SystemExit(f"{name}: not found")


def main():
    letters = set(sys.argv[1:])
    for name, edits in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        text = path.read_text()
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
            changed = True
        if changed:
            path.write_text(text)
            print(f"edited {module}/{name}")


if __name__ == "__main__":
    main()
