#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 4: agent-docs lines for the departed-permanent rebuilds that are gone.

Idempotent; run from the repository root:

    python3 scripts/step15-departed-rebuilds-docs.py

Each entry replaces the one line that starts with the given text, or deletes it when the replacement is None.
"""
import pathlib

DOCS = pathlib.Path("agent-docs")

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"At the beginning of your end step, put a coin counter on another target creature. Whenever a creature with a coin counter",
         "| \"At the beginning of your end step, put a coin counter on another target creature. Whenever a creature with a coin counter on it dies or is put into exile, return that card to the battlefield under your control.\" | `target(anotherCreature).addTrigger(Triggers.endStep(ActivePlayer.YOU), PutCounterOnTargetPermanentEffect(COIN, 1))` + `Triggers.creatureDies(new PermanentHasCountersPredicate(COIN), ANY)` / `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), AllOf(Triggers.CREATURE, PermanentHasCountersPredicate(COIN)), ANY)` with `ReturnTriggeringPermanentToBattlefieldEffect(Zone.GRAVEYARD / Zone.EXILE)` | Triggers.endStep(ActivePlayer.YOU) + creature-death/exile triggers | Athreos, Shroud-Veiled (DSC 212); \"with a coin counter on it\" is the triggers' filter, judged on the creature as it last existed on the battlefield (CR 603.10a), and the return handler binds the exact card identity |"),
        ("- \"When this creature dies, if it had a revival counter on it, return it to the battlefield with one fewer revival counter",
         "- \"When this creature dies, if it had a revival counter on it, return it to the battlefield with one fewer revival counter on it at the beginning of the next end step.\" -> `Triggers.dies(new PermanentHasCountersPredicate(CounterType.REVIVAL), THIS)` + `RegisterDelayedSelfReturnFromGraveyardEffect(CounterType.REVIVAL, new Sum(new CountersOnTriggeringPermanent(CounterType.REVIVAL), new Fixed(-1)))` (Nine-Lives Familiar). The intervening \"if\" is the trigger's filter; the amount is evaluated as the death trigger resolves, from the dying permanent as it last existed on the battlefield."),
        ("| \"Choose target creature you control. Until end of turn, all damage that would be dealt to you and creatures you control is dealt to the chosen creature instead.",
         '| "Choose target creature you control. Until end of turn, all damage that would be dealt to you and creatures you control is dealt to the chosen creature instead. When that creature dies this turn, put its counters on up to one target creature you control and draw a card." | `target(TargetFilters.creatureYouControl())` + `RedirectYourDamageToTargetCreatureThisTurnEffect()` + `ResolveEffectOnTargetDeathThisTurnEffect(SequenceEffect.upToOneTarget(new MoveDyingSourceCountersToTargetCreatureEffect(true), DrawCardEffect(1)))` | SPELL | Heroic Sacrifice (MSC 18). The redirect locks to the cast-time target; the moved counters are the dying target\'s as it last existed on the battlefield (CR 603.10a), and the delayed trigger keeps the counter transfer optional while the draw remains unconditional. |'),
        ("| \"When this creature dies, put a number of +1/+1 counters equal to its power on up to one target creature\"",
         "| \"When this creature dies, put a number of +1/+1 counters equal to its power on up to one target creature\" | `GrantEffectToTargetUntilEndOfTurnEffect(new TriggeredAbility(Triggers.dies(THIS), new PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, new SourcePower(), null, new PermanentIsCreaturePredicate(), false, null, true)))` | `SPELL` | Galuf's Final Act. `SourcePower` reads the dying creature's last-known power as the granted ability resolves; the last argument makes the creature target optional (\"up to one\") |"),
        ("| \"when this Equipment is put into a graveyard from the battlefield, exile equipped creature\"",
         "| \"when this Equipment is put into a graveyard from the battlefield, exile equipped creature\" | `ExileEquippedCreatureEffect()` | `Triggers.dies(THIS)` | Oathkeeper, Takeno's Daisho. Exiles the creature the stack entry's triggering permanent (the Equipment as it last existed on the battlefield, CR 603.10a) was attached to; an unattached Equipment still triggers and exiles nothing |"),
        ("| \"When this creature dies, create an X/X black Horror creature token, where X is this creature's power\"",
         "| \"When this creature dies, create an X/X black Horror creature token, where X is this creature's power\" | `CreateTokenWithDyingSourcePowerEffect(new CreateTokenEffect(\"Horror\", 0, 0, BLACK, List.of(HORROR), Set.of(), Set.of()))` | `Triggers.dies(THIS)` | The token's base power and toughness are the stack entry's event value, the dying creature's last-known power, which the death trigger records; the token gets no counters (Flesh Carver) |"),
        ("| \"When this creature dies, if it had two or more +1/+1 counters on it, create a token that's a copy of it",
         "| \"When this creature dies, if it had two or more +1/+1 counters on it, create a token that's a copy of it at the beginning of the next end step. The token enters with half that many counters\" | `RegisterDelayedCreateTokenCopyOfDyingSourceEffect(new Divided(new CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE), 2))` | `Triggers.dies(new PermanentCounterCountAtLeastPredicate(PLUS_ONE_PLUS_ONE, 2), THIS)` | The intervening \"if\" is the trigger's filter; the amount is evaluated as the death trigger resolves, from the dying creature as it last existed on the battlefield, and the delayed source copy enters with it (Ochre Jelly) |"),
        ("| \"When this dies, if it had counters on it, put those counters on up to one target creature\"",
         "| \"When this dies, if it had counters on it, put those counters on up to one target creature\" | `target(0, 1)` + `MoveDyingSourceCountersToTargetCreatureEffect()` | `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), THIS)` | Moves every counter of the stack entry's triggering permanent (the dying creature as it last existed on the battlefield, CR 603.10a) to a chosen creature; \"if it had counters on it\" (an intervening \"if\", CR 603.4) is the trigger's filter (e.g. Scolding Administrator; Iron Apprentice with `(true)` for \"target creature you control\") |"),
        ("| \"When this creature dies, put its counters on target creature you control\"",
         "| \"When this creature dies, put its counters on target creature you control\" | `MoveDyingSourceCountersToTargetCreatureEffect(true)` | `Triggers.dies(THIS)` | Places every counter of the stack entry's triggering permanent (the dying creature as it last existed on the battlefield, CR 603.10a) on the mandatory controlled-creature target; with no filter it triggers in the zero-counter case too (Parish-Blade Trainee) |"),
        ("| \"Whenever a creature you don't control with a [type] counter on it dies, create a [token]\"",
         "| \"Whenever a creature you don't control with a [type] counter on it dies, create a [token]\" | `new CreateTokenEffect(...)` | `Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.TYPE), OPPONENT_CONTROLS)` | The counter is the trigger's filter, judged on the dying creature as it last existed on the battlefield; one token however many counters it had (Toxrill). Use `CounterType.ANY` for \"with a counter on it\" (Village Pillagers, which ignores silver counters as every `PermanentHasCountersPredicate(ANY)` does). |"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `ExileEquippedCreatureEffect` |",
         "| `ExileEquippedCreatureEffect` | `()` | Triggers.dies(THIS) effect on an Equipment: exile the creature the stack entry's triggering permanent (the Equipment as it last existed on the battlefield, CR 603.10a) was attached to. An unattached Equipment still triggers and exiles nothing; nothing happens if that creature has left the battlefield. Used by Oathkeeper, Takeno's Daisho |"),
        ("| `CreateTokenWithDyingSourcePowerEffect` |",
         "| `CreateTokenWithDyingSourcePowerEffect` | `(CreateTokenEffect tokenTemplate)` | `Triggers.dies(THIS)` only: creates one copy of `tokenTemplate` with power and toughness equal to the stack entry's event value, the dying creature's last-known power the death trigger records (clamped to zero). The token gets no counters. Used by Flesh Carver |"),
        ("| `CreateTokenIfDyingSourceHadCounterEffect` |", None),
        ("| `MoveDyingSourceCountersToTargetCreatureEffect` | `(boolean controllerOnly, boolean requiresCounters)`",
         "| `MoveDyingSourceCountersToTargetCreatureEffect` | `()` / `(boolean controllerOnly)` | Moves every counter of the stack entry's triggering permanent (as it last existed on the battlefield, CR 603.10a) onto a target creature. \"If it had counters on it\" (an intervening \"if\", CR 603.4) is the trigger's filter, `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), THIS)`; without it the ability triggers and places no counters. `controllerOnly` restricts the target to a creature controlled by the trigger's controller. Used by Scolding Administrator, Iron Apprentice, Host of the Hereafter, and Parish-Blade Trainee |"),
        ("| `TargetPlayerLosesLifeEqualToPowerEffect` |", None),
        ("| `SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect` |",
         "| `SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect` | `(int power, int toughness)` | Untargeted `Triggers.dies(THIS)` effect: each creature still on the battlefield that dealt damage to the source this turn gets the given base power and toughness indefinitely. The damage sources are the ones the stack entry's triggering permanent (the source as it last existed on the battlefield, CR 603.10a) records, so the ability still applies after the dying source leaves the graveyard. Delegates each creature to `SetBasePowerToughnessEffectHandler` with an indefinite duration. Used by Brine Hag |"),
        ("| `PutCountersOnTargetForEachDyingSourcePowerEffect` |", None),
        ("| `RegisterDelayedCreateTokenCopyOfDyingSourceEffect` |",
         "| `RegisterDelayedCreateTokenCopyOfDyingSourceEffect` | `(DynamicAmount plusOnePlusOneCounters)` | Schedules a next-end-step token copy of the source that enters with `plusOnePlusOneCounters` +1/+1 counters, evaluated as the death trigger resolves. Ochre Jelly: `new Divided(new CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE), 2)` on `Triggers.dies(new PermanentCounterCountAtLeastPredicate(PLUS_ONE_PLUS_ONE, 2), THIS)` |"),
        ("| `GainLifeEqualToDyingSourcePowerEffect` |", None),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ReturnTriggeringPermanentToBattlefieldWithCounterEffect(CounterType, Zone)`",
         "- `ReturnTriggeringPermanentToBattlefieldEffect(Zone)` — returns the card of the creature that triggered the ability from the graveyard or exile it was put into to the triggered ability controller's battlefield; which creatures count is the trigger's filter (\"a creature with a coin counter on it\": `PermanentHasCountersPredicate(COIN)`); used by Athreos, Shroud-Veiled (DSC 212)"),
        ("- `RegisterDelayedSelfReturnFromGraveyardEffect(CounterType, int)`",
         "- `RegisterDelayedSelfReturnFromGraveyardEffect(CounterType, int)` or `(CounterType, DynamicAmount)` — registers a delayed end-step return of the SOURCE CARD from its owner's graveyard to the battlefield under that owner's control, entering with the given counters (`()` = no counters; a `DynamicAmount` is evaluated as the registering ability resolves). Sand Golem = `Triggers.opponentCausesYouToDiscardThisCard()` + `(PLUS_ONE_PLUS_ONE, 1)`. Nine-Lives Familiar's \"if it had a revival counter on it ... with one fewer revival counter\" = `Triggers.dies(new PermanentHasCountersPredicate(REVIVAL), THIS)` + `(REVIVAL, new Sum(new CountersOnTriggeringPermanent(REVIVAL), new Fixed(-1)))`. Fizzles if the card isn't in a graveyard when the trigger resolves; the delayed return itself is skipped if the card has left by the end step. Untransformed sibling of `RegisterDelayedReturnSourceTransformedEffect`."),
        ("- `RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect(CounterType)`", None),
        ("- `RegisterDelayedCreateTokenCopyOfDyingSourceEffect(int minimumCounterCount, int counterDivisor)`",
         "- `RegisterDelayedCreateTokenCopyOfDyingSourceEffect(DynamicAmount plusOnePlusOneCounters)` — registers a next-end-step token copy of the source that enters with the given +1/+1 counters, evaluated as the death trigger resolves. Ochre Jelly: `new Divided(new CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE), 2)` on `Triggers.dies(new PermanentCounterCountAtLeastPredicate(PLUS_ONE_PLUS_ONE, 2), THIS)`."),
        ("- `ExileEquippedCreatureEffect()`",
         "- `ExileEquippedCreatureEffect()` — Triggers.dies(THIS) on an Equipment: exile the creature the Equipment, as it last existed on the battlefield, was attached to; an unattached Equipment still triggers and exiles nothing (Oathkeeper, Takeno's Daisho)"),
        ("- `CreateTokenWithDyingSourcePowerEffect(CreateTokenEffect template)`",
         "- `CreateTokenWithDyingSourcePowerEffect(CreateTokenEffect template)` — `Triggers.dies(THIS)`: create one `template` token with base power and toughness equal to the stack entry's event value, the dying creature's last-known power (clamped to zero), and no counters (e.g. Flesh Carver)"),
        ("- `CreateTokenIfDyingSourceHadCounterEffect(", None),
        ("- `MoveDyingSourceCountersToTargetCreatureEffect([boolean controllerOnly])`",
         "- `MoveDyingSourceCountersToTargetCreatureEffect([boolean controllerOnly])` — move all of the dying creature's counters onto a target creature. The counters are the stack entry's triggering permanent's (as it last existed on the battlefield, CR 603.10a); `controllerOnly` restricts the target to a creature controlled by the trigger's controller. \"If it had counters on it\" (an intervening \"if\", CR 603.4) is the trigger's filter, `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), THIS)` (Iron Apprentice, Scolding Administrator); without it the ability triggers with no counters too (Parish-Blade Trainee)"),
        ("- `SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect(int power, int toughness)`",
         "- `SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect(int power, int toughness)` — untargeted Triggers.dies(THIS) effect: each creature still on the battlefield that dealt damage to the source this turn has its base power and toughness set indefinitely. The damage sources are the ones the dying source, as it last existed on the battlefield, records, so the ability still applies after it leaves the graveyard (Brine Hag)"),
        ("- `PutCountersOnTargetForEachDyingSourcePowerEffect()`", None),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| Boost + subtype-conditional reanimate + exile-on-own-death equip |",
         "| Boost + subtype-conditional reanimate + exile-on-own-death equip | `o/OathkeeperTakenosDaisho.java` | STATIC StaticBoostEffect(3, 1, EQUIPPED_CREATURE) + Triggers.creatureDies(EQUIPPED) ConditionalEffect(TriggeringCardMatches(CardSubtypePredicate(SAMURAI)), ReturnDyingCreatureToBattlefieldEffect(false)) + Triggers.dies(THIS) ExileEquippedCreatureEffect + EquipActivatedAbility(\"{2}\"). The reanimate puts the Samurai back under the Equipment controller's control without reattaching; the Triggers.dies(THIS) half exiles the creature the Equipment, as it last existed on the battlefield, was attached to |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Dies with at least two +1/+1 counters -> create a delayed copy with half that many counters |",
         "| Dies with at least two +1/+1 counters -> create a delayed copy with half that many counters | `o/OchreJelly.java` | Triggers.dies(PermanentCounterCountAtLeastPredicate(PLUS_ONE_PLUS_ONE, 2), THIS) `RegisterDelayedCreateTokenCopyOfDyingSourceEffect(new Divided(new CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE), 2))` registers a next-end-step source copy that enters with the rounded-down half of the dying creature's last-known counters |"),
        ("| CU + gain life equal to its own age counters on leave |",
         "| CU + gain life equal to its own age counters on leave | `r/ReveredUnicorn.java` | Triggers.upkeep(ActivePlayer.YOU) `CumulativeUpkeepEffect(\"{1}\")` + Triggers.leavesBattlefield(THIS) `GainLifeEffect(new CountersOnTriggeringPermanent(AGE))`, the leaving permanent's counters as it last existed on the battlefield |"),
        ("| Dies with a counter — self-reanimate at end step with one fewer |",
         "| Dies with a counter — self-reanimate at end step with one fewer | `n/NineLivesFamiliar.java` | Triggers.dies(PermanentHasCountersPredicate(REVIVAL), THIS) `RegisterDelayedSelfReturnFromGraveyardEffect(REVIVAL, new Sum(new CountersOnTriggeringPermanent(REVIVAL), new Fixed(-1)))` — the filter is \"if it had a revival counter on it\"; the amount reads the dying permanent's last-known counters as the trigger resolves |"),
    ],
}


def main():
    changed = []
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        original = list(lines)
        for prefix, replacement in edits:
            matches = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if replacement is not None and replacement in lines:
                continue
            if not matches:
                if replacement is None:
                    continue
                raise SystemExit("%s: no line starts with %r" % (name, prefix))
            if len(matches) > 1:
                raise SystemExit("%s: %d lines start with %r" % (name, len(matches), prefix))
            if replacement is None:
                del lines[matches[0]]
            else:
                lines[matches[0]] = replacement
        if lines != original:
            path.write_text("\n".join(lines))
            changed.append(name)
    print("\n".join(changed) if changed else "nothing to change")


if __name__ == "__main__":
    main()
