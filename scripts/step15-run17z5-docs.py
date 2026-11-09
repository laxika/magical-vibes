#!/usr/bin/env python3
"""Step 15, run 17z5: the agent-docs lines that name the removed draw records (`RevealFirstDrawDrawOnBasicLandEffect`,
`BoobyTrapEffect`, `FirstDrawRevealTriggerEffect` and its four implementers, `DrawRevealTriggerEffect`,
`RevealEachDrawEffect`, `DrawnCardTriggerEffect`, `PerpetuallyBoostDrawnDragonEffect`,
`RevealDrawnCardIfInstantOrSorceryEffect`), the static reveal `RevealDrawnCardsEffect`, the reveal trigger
`Triggers.youRevealCardThisWay()`, the draw triggers' drawn-card `withFilter`, the condition `TargetPlayerIsChosenPlayer`,
`PerpetuallyBoostCardEffect.ofTriggeringCard` and the fieldless `CopyDrawnInstantOrSorceryAndMayCastCopyEffect()`.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
record outside a "Gone" line.

Usage: step15-run17z5-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17z5"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
PI = "agent-docs/CARD_PATTERN_INDEX.md"
CP = "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md"
PC = "agent-docs/PLANECHASE.md"
TS = "agent-docs/TRIGGER_SLOT_TARGETING.md"
MP = "agent-docs/EFFECT_MERGE_PLAN.md"
AG = "agent-docs/ACTIVATED_ABILITY_GUIDE.md"

BASIC = ("new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.LAND), "
         "new CardSupertypePredicate(CardSupertype.BASIC)))")
IOS = ("new CardAnyOfPredicate(List.of(new CardTypePredicate(CardType.INSTANT), "
       "new CardTypePredicate(CardType.SORCERY)))")
REVEAL_TEXT = ("static ability: a player reveals the cards they draw as they draw them (CR 701.20a), detected by "
               "`DrawService` as each card is drawn: `firstEachTurn()` (\"reveal the first card you draw each turn\"), "
               "`firstOnEachOfYourTurns()`, `mayRevealFirstEachTurn()` (\"you may reveal … as you draw it\": asked "
               "during the draw, CR 121.9, pausing a multi-card draw), `eachByChosenPlayer()` (the player chosen by "
               "`ChooseOpponentOnEnterEffect`), `eachByEveryPlayer()` (also from a face-up plane). A card a permanent "
               "reveals this way triggers that permanent's `Triggers.youRevealCardThisWay().withFilter(CardPredicate)` "
               "abilities (\"whenever you reveal a [card] this way\"), the revealed card the entry's triggering card. "
               "Rowen, Primitive Etchings, Keranos, God-Eternal Kefnet, Inquisitor Eisenhorn, Booby Trap, Sea of Sand")


def gone(name, now):
    return f"**Gone** ({RUN}): {now}"


EDITS = [
    (OM, "| \"Whenever you draw a Dragon card, it perpetually gets +1/+1.",
     "| \"Whenever you draw a Dragon card, it perpetually gets +1/+1. When Darigaaz's Whelp enters, if it was kicked, "
     "seek a Dragon card. Darigaaz's Whelp and that Dragon card each perpetually get +1/+1.\" | "
     "`Triggers.youDrawCard().withFilter(new CardSubtypePredicate(CardSubtype.DRAGON))` + "
     "`PerpetuallyBoostCardEffect.ofTriggeringCard(1, 1)` + kicked `Triggers.enters(ObjectRelation.THIS) "
     "SeekDragonAndPerpetuallyBoostEffect()` | draw trigger + ETB | Darigaaz's Whelp (YDMU 8); the draw collector judges "
     "the filter on the drawn card without revealing it and records the card as the entry's triggering card, and the "
     "kicked seek chooses randomly from the controller's library. |", "replace"),
    (OM, "| \"Reveal the first card you draw on each of your turns.",
     "| \"Reveal the first card you draw on each of your turns. Whenever you reveal a land card this way, draw a card. "
     "Whenever you reveal a nonland card this way, [this] deals 3 damage to any target.\" | "
     "`RevealDrawnCardsEffect.firstOnEachOfYourTurns()` (static) + `Triggers.youRevealCardThisWay().withFilter(new "
     "CardTypePredicate(CardType.LAND))` `DrawCardEffect(1)` + `Triggers.youRevealCardThisWay().withFilter(new "
     "CardNotPredicate(new CardTypePredicate(CardType.LAND)))` `DealDamageToAnyTargetEffect(3)` | static + reveal "
     "triggers | Keranos, God of Storms. The reveal applies to the controller's first draw on the controller's turn; "
     "the nonland ability's target is chosen through the draw-trigger any-target choice |", "replace"),
    (OM, "| \"Reveal the first card you draw each turn. Whenever you reveal a creature card this way, draw a card.\"",
     "| \"Reveal the first card you draw each turn. Whenever you reveal a creature card this way, draw a card.\" | "
     "`RevealDrawnCardsEffect.firstEachTurn()` (static) + `Triggers.youRevealCardThisWay().withFilter(new "
     "CardTypePredicate(CardType.CREATURE))` `DrawCardEffect(1)` | static + reveal trigger | Primitive Etchings. Applies "
     "on any turn when its controller draws; the additional draw isn't the turn's first, so it isn't revealed |",
     "replace"),
    (OM, "| \"You may reveal the first card you draw each turn ... copy that card",
     "| \"You may reveal the first card you draw each turn ... copy that card ... That copy costs {2} less to cast\" | "
     f"`RevealDrawnCardsEffect.mayRevealFirstEachTurn()` (static) + `Triggers.youRevealCardThisWay().withFilter({IOS})` "
     "`CopyDrawnInstantOrSorceryAndMayCastCopyEffect()` | static + reveal trigger | God-Eternal Kefnet. The reveal "
     "choice is made during the draw (CR 121.9); the ability triggers only for a revealed instant or sorcery, copies the "
     "entry's triggering card (its last-known information if it left the hand), parks the copy in exile with "
     "`ReduceOwnCastCostEffect(new Fixed(2))`, and a second may-choice offers it for its reduced cost |", "replace"),
    (OM, "| \"You may reveal the first card you draw each turn as you draw it. Whenever you reveal an instant or sorcery card this way, create Cherubael",
     "| \"You may reveal the first card you draw each turn as you draw it. Whenever you reveal an instant or sorcery "
     "card this way, create Cherubael ...\" | `RevealDrawnCardsEffect.mayRevealFirstEachTurn()` (static) + "
     f"`Triggers.youRevealCardThisWay().withFilter({IOS})` `CreateTokenEffect cherubael` | static + reveal trigger | "
     "Inquisitor Eisenhorn (40K 127). The reveal choice is offered for the first card drawn on any player's turn, "
     "whatever its type; only an instant or sorcery triggers the ability |", "replace"),
    (OM, "| \"Players reveal each card they draw. Whenever a player draws a land card",
     "| \"Players reveal each card they draw. Whenever a player draws a land card, that player gains 3 life. Whenever a "
     "player draws a nonland card, that player loses 3 life. Whenever chaos ensues, put target permanent on top of its "
     "owner's library.\" | `RevealDrawnCardsEffect.eachByEveryPlayer()` (static) + `List.of(Triggers.youDrawCard()"
     ".withFilter(land), Triggers.opponentDrawsCard().withFilter(land))` `GainLifeEffect(3, TRIGGERING_PLAYER)` + the "
     "same with `CardNotPredicate(land)` `LoseLifeEffect(3, TRIGGERING_PLAYER)` + `Triggers.chaosEnsues() "
     "PutTargetOnTopOfLibraryEffect()` | PLANAR | Sea of Sand (OHOP 33). The planar draw collector judges the filter "
     "on the drawn card for either drawer; the drawing player is the non-targeting trigger context. |", "replace"),
    (OM, "| \"As this enters, choose an opponent and a card name … The chosen player reveals each card they draw.",
     "| \"As this enters, choose an opponent and a card name … The chosen player reveals each card they draw. When they "
     "draw a card with the chosen name, sacrifice this and it deals 10 damage to that player\" | "
     "`ChooseCardNameOnEnterEffect(…)` + `ChooseOpponentOnEnterEffect()` + `RevealDrawnCardsEffect.eachByChosenPlayer()` "
     "(static) + `List.of(Triggers.opponentDrawsCard().withFilter(new CardHasSourceChosenNamePredicate()), "
     "Triggers.youDrawCard().withFilter(new CardHasSourceChosenNamePredicate()))` with "
     "`TriggerOptions.DEFAULT.withEventCondition(new TargetPlayerIsChosenPlayer())` + `SacrificeSelfThenEffect(new "
     "DealDamageToPlayersEffect(10, DamageRecipient.TRIGGERING_PLAYER))` | static + draw trigger | Booby Trap. Both "
     "relations so the chosen player triggers it whoever controls the trap; the name is judged on the drawn card |",
     "replace"),
    (OM, "| \"Reveal the first card you draw each turn. Whenever you reveal a basic land card this way, draw a card\"",
     "| \"Reveal the first card you draw each turn. Whenever you reveal a basic land card this way, draw a card\" | "
     f"`RevealDrawnCardsEffect.firstEachTurn()` + `Triggers.youRevealCardThisWay().withFilter({BASIC})` "
     "`DrawCardEffect(1)` | static + reveal trigger | Rowen. The reveal is the turn's first draw only, so the extra draw "
     "isn't revealed |", "replace"),
    (QR, "- `DrawnCardTriggerEffect` - ",
     "- `DrawnCardTriggerEffect` / `PerpetuallyBoostDrawnDragonEffect` — " + gone("", "a draw trigger's filter on the "
     "drawn card is `Triggers.youDrawCard().withFilter(CardPredicate)`, judged by the draw collector, the drawn card the "
     "entry's triggering card; Darigaaz's Whelp is `withFilter(new CardSubtypePredicate(DRAGON))` + "
     "`PerpetuallyBoostCardEffect.ofTriggeringCard(1, 1)`"), "replace"),
    (QR, "- `PerpetuallyBoostCardEffect(Card card, DynamicAmount powerBoost",
     "- `PerpetuallyBoostCardEffect(Card card, DynamicAmount powerBoost, DynamicAmount toughnessBoost)` - perpetually "
     "modifies a physical card's P/T across zone changes, applying the stored modifier whenever that card enters the "
     "battlefield; fixed integer overloads remain available. `ofTriggeringPermanent(p, t)` / `ofTriggeringCard(p, t)` "
     "(the same record, no card) boost the stack entry's triggering permanent's card, else its triggering card (a drawn "
     "card)", "replace"),
    (QR, "- `RevealFirstDrawEffect(CardEffect onLand",
     "- `RevealDrawnCardsEffect` - " + REVEAL_TEXT, "replace"),
    (QR, "- `RevealFirstDrawCreatureEffect()` - ",
     "- `RevealFirstDrawEffect` / `RevealFirstDrawCreatureEffect` / `RevealFirstDrawInstantOrSorceryEffect` / "
     "`RevealFirstDrawInstantOrSorceryCreateTokenEffect` / `RevealDrawnCardIfInstantOrSorceryEffect` — " + gone("",
     "the reveal is the static `RevealDrawnCardsEffect` and \"whenever you reveal a [card] this way\" "
     "`Triggers.youRevealCardThisWay().withFilter(CardPredicate)` with the card's own effect"), "replace"),
    (QR, "- `RevealFirstDrawInstantOrSorceryEffect()` - offers", None, "delete"),
    (QR, "- `RevealFirstDrawInstantOrSorceryCreateTokenEffect(CreateTokenEffect tokenEffect)` - ", None, "delete"),
    (QR, "- `RevealEachDrawEffect(CardEffect onLand",
     "- `RevealEachDrawEffect` — " + gone("", "Sea of Sand is `RevealDrawnCardsEffect.eachByEveryPlayer()` and two draw "
     "abilities filtered on the drawn card (land / nonland)"), "replace"),
    (QR, "- `BoobyTrapEffect()` — STATIC marker",
     "- `BoobyTrapEffect` — " + gone("", "Booby Trap is `RevealDrawnCardsEffect.eachByChosenPlayer()` and a draw "
     "ability filtered by `CardHasSourceChosenNamePredicate` with the event condition `TargetPlayerIsChosenPlayer`"),
     "replace"),
    (QR, "- `RevealFirstDrawDrawOnBasicLandEffect()` — STATIC marker",
     "- `RevealFirstDrawDrawOnBasicLandEffect` — " + gone("", "Rowen is `RevealDrawnCardsEffect.firstEachTurn()` and "
     "`Triggers.youRevealCardThisWay().withFilter(basic land)` + `DrawCardEffect(1)`"), "replace"),
    (QR, "- `TargetPlayerIsChosenPlayer()` is",
     "- `TargetPlayerIsChosenPlayer()` is \"that player is the chosen player\" of `ChooseOpponentOnEnterEffect` (the "
     "source's chosen player ids): as a draw trigger's event condition it reads the drawing player (Booby Trap's \"when "
     "the chosen player draws\").", "after-anchor:- `TargetPlayerIs(UUID playerId)` is"),
    (IX, "| `DrawnCardTriggerEffect` |",
     "| `DrawnCardTriggerEffect` | — | " + gone("", "`Triggers.youDrawCard().withFilter(CardPredicate)`, the drawn "
     "card the entry's triggering card (Darigaaz's Whelp: `PerpetuallyBoostCardEffect.ofTriggeringCard(1, 1)`)") + " |",
     "replace"),
    (IX, "| `PerpetuallyBoostCardEffect` |",
     "| `PerpetuallyBoostCardEffect` | `(Card card, DynamicAmount powerBoost, DynamicAmount toughnessBoost)` | records a "
     "perpetual P/T modifier for the physical card identity and applies it to matching permanents when they enter the "
     "battlefield; fixed integer overloads remain available; `ofTriggeringPermanent(p, t)` / `ofTriggeringCard(p, t)` "
     "boost the stack entry's triggering permanent's card, else its triggering card. Used by Darigaaz's Whelp (YDMU 8), "
     "Tiana, Angelic Mechanic and Arvad, Weatherlight Smuggler (YDMU 20). |", "replace"),
    (IX, "| `RevealFirstDrawEffect` |",
     "| `RevealDrawnCardsEffect` | `(Revealer revealer, boolean firstCardEachTurn, boolean yourTurnsOnly, boolean "
     "optional)` | " + REVEAL_TEXT + " |", "replace"),
    (IX, "| `RevealFirstDrawCreatureEffect` |",
     "| `RevealFirstDrawEffect` / `RevealFirstDrawCreatureEffect` | — | " + gone("", "`RevealDrawnCardsEffect` + "
     "`Triggers.youRevealCardThisWay().withFilter(...)` abilities (Keranos, Primitive Etchings)") + " |", "replace"),
    (IX, "| `RevealFirstDrawInstantOrSorceryEffect` |",
     "| `RevealFirstDrawInstantOrSorceryEffect` / `RevealFirstDrawInstantOrSorceryCreateTokenEffect` | — | " + gone("",
     "`RevealDrawnCardsEffect.mayRevealFirstEachTurn()` + `Triggers.youRevealCardThisWay().withFilter(instant or "
     "sorcery)` (God-Eternal Kefnet's `CopyDrawnInstantOrSorceryAndMayCastCopyEffect()`, Inquisitor Eisenhorn's "
     "`CreateTokenEffect`)") + " |", "replace"),
    (IX, "| `RevealFirstDrawInstantOrSorceryCreateTokenEffect` |", None, "delete"),
    (IX, "| `RevealDrawnCardIfInstantOrSorceryEffect` |",
     "| `RevealDrawnCardIfInstantOrSorceryEffect` | — | " + gone("", "the optional reveal is the pending may of "
     "`RevealDrawnCardsEffect` itself (`RevealDrawnCardsHandler`), and the ability it triggers is the card's own "
     "`youRevealCardThisWay()` ability") + " |", "replace"),
    (IX, "| `RevealEachDrawEffect` |",
     "| `RevealEachDrawEffect` | — | " + gone("", "Sea of Sand is `RevealDrawnCardsEffect.eachByEveryPlayer()` and draw "
     "abilities filtered on the drawn card") + " |", "replace"),
    (IX, "| `CopyDrawnInstantOrSorceryAndMayCastCopyEffect` |",
     "| `CopyDrawnInstantOrSorceryAndMayCastCopyEffect` | `()` | God-Eternal Kefnet's revealed-card copy: copies the "
     "stack entry's triggering card (the revealed card as it was, its last-known information if it left the hand), "
     "creates the copy in exile with `ReduceOwnCastCostEffect(new Fixed(2))` and offers to cast it; the may handler "
     "pays the reduced cost and reuses `ExileFreeCastQueueSupport` |", "replace"),
    (IX, "| `BoobyTrapEffect` |",
     "| `BoobyTrapEffect` | — | " + gone("", "Booby Trap is `RevealDrawnCardsEffect.eachByChosenPlayer()` and a draw "
     "ability (`opponentDrawsCard()` and `youDrawCard()`, each `.withFilter(new CardHasSourceChosenNamePredicate())`) "
     "with the event condition `TargetPlayerIsChosenPlayer` and `SacrificeSelfThenEffect(new DealDamageToPlayersEffect("
     "10, DamageRecipient.TRIGGERING_PLAYER))`") + " |", "replace"),
    (IX, "| `RevealFirstDrawDrawOnBasicLandEffect` |",
     "| `RevealFirstDrawDrawOnBasicLandEffect` | — | " + gone("", "Rowen is `RevealDrawnCardsEffect.firstEachTurn()` and "
     "`Triggers.youRevealCardThisWay().withFilter(basic land)` + `DrawCardEffect(1)`") + " |", "replace"),
    (PI, "| trigger from the card actually drawn without revealing it |",
     "| trigger from the card actually drawn without revealing it | `Triggers.youDrawCard().withFilter(CardPredicate)` "
     "(the drawn card is the entry's triggering card) |", "replace"),
    (PI, "| \"reveal the first card you draw\" / \"whenever you reveal a [card] this way\" |",
     "| \"reveal the first card you draw\" / \"whenever you reveal a [card] this way\" | `RevealDrawnCardsEffect` + "
     "`Triggers.youRevealCardThisWay().withFilter(CardPredicate)` |",
     "after-anchor:| trigger from the card actually drawn without revealing it |"),
    (CP, "| Controller draws — gated on the drawn card |",
     "| Controller draws — gated on the drawn card | `d/DarigaazsWhelp.java`, `r/Rowen.java`, `b/BoobyTrap.java` | "
     "`Triggers.youDrawCard().withFilter(CardPredicate)` (the drawn card the entry's triggering card, "
     "`PerpetuallyBoostCardEffect.ofTriggeringCard`); a revealed first draw is the static `RevealDrawnCardsEffect` + "
     "`Triggers.youRevealCardThisWay().withFilter(CardPredicate)`; \"the chosen player draws\" both relations with the "
     "event condition `TargetPlayerIsChosenPlayer` |",
     "after-anchor:| Opponent discards — gated on the discarded card's type |"),
    (PC, "- Draw-trigger slots on face-up planar objects",
     "- Draw abilities on face-up planar objects can watch either the planar controller's or an opponent's actual draw "
     "and filter the drawn card (`withFilter`, judged by the planar draw collector); Sea of Sand writes "
     "`RevealDrawnCardsEffect.eachByEveryPlayer()` and land / nonland abilities, with the drawing player as trigger "
     "context and the drawn card as the triggering card.", "replace"),
    (TS, "A draw ability's `withFilter(CardPredicate)`",
     "A draw ability's `withFilter(CardPredicate)` (\"whenever you draw a Dragon card\") is judged by the draw collectors "
     "on the drawn card as it is drawn, the card the entry's triggering card; \"whenever you reveal a [card] this way\" "
     "(`Triggers.youRevealCardThisWay()`) is collected for the permanent whose `RevealDrawnCardsEffect` revealed it, "
     "with the same stack and any-target handling.",
     "after-anchor:exactly that number, with the same stack and any-target handling."),
    (AG, "| `Triggers.youRevealCardThisWay()` |",
     "| `Triggers.youRevealCardThisWay()` | \"Whenever you reveal a [card] this way\": a card the controller drew and "
     "this permanent's `RevealDrawnCardsEffect` revealed as it was drawn (Rowen, Keranos, God-Eternal Kefnet); narrow it "
     "with `withFilter(CardPredicate)`, judged on the revealed card, the card the entry's triggering card. A draw "
     "trigger's `withFilter` is judged on the drawn card the same way (Darigaaz's Whelp's \"whenever you draw a Dragon "
     "card\") |", "after-anchor:| `Triggers.enchantedPlayerDrawsCard()` |"),
    (MP, "  at runtime for Booby Trap's draw trigger (",
     None, "fragment:(`BoobyTrapEffect` is only the static marker)=>(its static marker is gone since " + RUN + ")"),
]

BLOCKS = []

GONE = ["RevealFirstDrawDrawOnBasicLandEffect", "BoobyTrapEffect", "FirstDrawRevealTriggerEffect",
        "DrawRevealTriggerEffect", "DrawnCardTriggerEffect", "RevealFirstDrawEffect", "RevealFirstDrawCreatureEffect",
        "RevealFirstDrawInstantOrSorceryEffect", "RevealFirstDrawInstantOrSorceryCreateTokenEffect", "RevealEachDrawEffect",
        "RevealDrawnCardIfInstantOrSorceryEffect", "PerpetuallyBoostDrawnDragonEffect"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "delete":
        if not idx:
            return False
        del lines[idx[0]]
        return True
    if mode == "after" or mode.startswith("after-anchor:"):
        if new in lines:
            return False
        anchor = mode[len("after-anchor:"):] if mode.startswith("after-anchor:") else start
        at = [i for i, line in enumerate(lines) if line.startswith(anchor)]
        if not at:
            raise SystemExit(f"anchor not found: {anchor}")
        lines.insert(at[0] + 1, new)
        return True
    if mode.startswith("fragment:"):
        old, repl = mode[len("fragment:"):].split("=>", 1)
        for i in idx:
            if repl in lines[i]:
                return False
            if old in lines[i]:
                lines[i] = lines[i].replace(old, repl, 1)
                return True
        raise SystemExit(f"fragment not found under {start}: {old}")
    if new in lines:
        return False
    if not idx:
        raise SystemExit(f"line not found: {start}")
    lines[idx[0]] = new
    return True


def main():
    for path, old, new in BLOCKS:
        file = ROOT / path
        text = file.read_text()
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"block not found in {path}: {old[:60]}")
        file.write_text(text.replace(old, new))
        print(f"updated {path}")
    by_file = {}
    for path, start, new, mode in EDITS:
        by_file.setdefault(path, []).append((start, new, mode))
    for path, edits in by_file.items():
        file = ROOT / path
        lines = file.read_text().split("\n")
        changed = False
        for start, new, mode in edits:
            changed |= apply(lines, start, new, mode)
        if changed:
            file.write_text("\n".join(lines))
            print(f"updated {path}")
    docs = list((ROOT / "agent-docs").glob("*.md")) + list((ROOT / ".claude/skills").rglob("SKILL.md")) \
        + list((ROOT / ".codex/skills").rglob("SKILL.md"))
    for doc in docs:
        for number, line in enumerate(doc.read_text().split("\n"), 1):
            for gone in GONE:
                if re.search(r"\b" + gone + r"\b", line) and "**Gone**" not in line:
                    raise SystemExit(f"{doc.relative_to(ROOT)}:{number} still names {gone}")


if __name__ == "__main__":
    main()
