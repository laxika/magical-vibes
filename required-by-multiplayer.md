# Required by multiplayer

Rules defects and missing features that only show in a game with more than two players, or that need the engine,
the view or the client to model more than one opponent. Everything here waits until proper multiplayer is
implemented; don't fix an entry before then.

To add an entry, give it the next number and append it at the bottom. Remove an entry once multiplayer has landed
and the entry's behaviour is right.

---

## 1. A player who loses leaves the game while the others play on

**Found:** 2026-11-27, rules-fixes run MP-2 (refactors-to-do.md entry 18, line M3); moved here 2026-11-28 by the owner

**Problem.** Every loss ends the game (`ControllerLosesGameEffectHandler`, `GameOutcomeService.checkWinCondition`,
`declareWinner` with `getOpponentId` of the loser). There is no model of a player leaving while the others play on
(CR 104.2a, 800.4a). So Triskaidekaphobia's "each player with exactly 13 life loses" can't eliminate one of three
players, and declares the first survivor the winner.

**Needs.** A player-elimination model: the leaving player's objects leave the game, control effects that give them
objects end (CR 800.4a), priority passes on, and the game ends only when one player is left (CR 104.2a).

## 2. A revealed opponent hand is shown for one opponent only

**Found:** 2026-11-27, rules-fixes run MP-2 (refactors-to-do.md entry 18, line H7); moved here 2026-11-28 by the owner

**Problem.** `GameViewProjectionFactory.getRevealedOpponentHand` (Zur's Weirding, Telepathy) returns only the first
opponent's hand, since `GameStateMessage` and the frontend have one opponent hand slot.

**Needs.** A hand per opponent in the game view and the client.

## 3. The gift goes to "the" opponent, not to the opponent chosen as the gift cost

**Found:** 2026-11-23, effect cleanup step 9 (`missing-features-to-do.md` entry 80, refactors-to-do.md entry 18 line M9);
moved here 2026-11-28 by the owner. Its two-player part was fixed then: Cruelclaw's Heist, Consumed by Greed and Perch
Protection give their gift without targeting, and every gift instant or sorcery gives its gift as it resolves.

**Problem.** CR 702.174a: gift's first ability is "As an additional cost to cast this spell, you may choose an
opponent", and each gift's effect goes to "the chosen player". The rulings say a copy of the spell has the gift
promised to the same opponent. The engine records only that the gift was promised (`StackEntry.giftPromised`, copied
to `Permanent.giftPromised`), never which opponent, so with three or more players:
- `TargetOpponentCreatesTokenEffect` (Fish, Food, Treasure, Octopus) and `ConjureCardToOpponentBattlefieldEffect`
  (Archival Whorl) give it to `GameQueryService.getOpponentId`, one opponent picked by the engine.
- `EachOtherPlayerDrawsCardEffect` ("gift a card": Cruelclaw's Heist and Consumed by Greed too) makes every other
  player draw.
- Perch Protection's `ExtraTurnEffect(1, Subjects.eachOpponent())` gives every opponent an extra turn.
Two ledger rows wait for it (`EachOtherPlayerDrawsCardEffect`, `TargetOpponentCreatesTokenEffect`).

**Needs.** The gift choice as a real additional-cost choice: the cast protocol and the client's cast flow gain an
opponent choice (today a boolean), the AI picks one, and `StackEntry` / `Permanent` carry the chosen player through
copies (the same opponent), the permanent's enters trigger and the opponent leaving the game (CR 800.4a). Every gift
effect then reads one "the chosen gift player" subject. Choosing the opponent isn't targeting, so hexproof or
protection doesn't stop it. Tests: in a three-player game, a "gift a card" spell (Dewdrop Cure) and a "gift a tapped
Fish" spell (Into the Flood Maw) promised to one opponent give only that opponent the gift.

## 4. "Other attacking creatures get" leaves out a teammate's attackers

**Found:** 2026-11-28, rules-fixes run Z (refactors-to-do.md entry 18, lines O93 and O97); moved here 2026-11-28 by the owner

**Problem.** Battle cry and the "attacking creatures get" / "other attacking creatures get" boosts are built over the creatures
their controller controls: battle cry on "creatures you control" (B2 keeps it), and Iroh, Firebending Instructor, Kangee's
Lieutenant, Motivated Pony, Targ Nar, Honored Crop-Captain and others on `BoostEffect.creaturesYouControl(…, attacking)`. CR
702.91a gives battle cry's +1/+0 to "each other attacking creature", so in a Two-Headed Giant game (CR 810.1) a teammate's
attacking creatures get it too, and the same goes for the "attacking creatures get" cards. With no team play the two forms give
the same result.

**Needs.** Team play (Two-Headed Giant's shared turns and attacks). Then these effects say what their text says,
`BoostEffect.allCreatures(…, attacking)` (an opponent's creatures can't be attacking on your turn), each with a test where a
teammate's attacker gets the boost.

## 5. Only the first opponent declares blockers

**Found:** 2026-11-27, rules-fixes run CB-1 (refactors-to-do.md entry 18, line M11); moved here 2026-11-28 by the owner. CB-1
fixed the `DefendingPlayer…` conditions.

**Problem.** `CombatBlockService.handleDeclareBlockersStep`, `declareBlockers` and `getBlockableAttackerIndices` take the
defending player as `getOpponentId`. So only the first opponent declares blockers, and that opponent may block every attacker,
including the ones attacking another player.

**Needs.** Each defending player declares blockers in APNAP order (CR 802.4), with only the creatures they control and only
against the creatures attacking them, their planeswalkers or the battles they protect (CR 802.4a, 802.4b). That needs
`CombatDamageService` to read more than one defending battlefield first (`buildBlockerMap` and its index-keyed damage state use
`getOpponentId`'s), then the AI and the frontend. To settle as it is built: whether several defenders' blocks are one "one or
more creatures become blocked" event.

## 6. Sword-Point Diplomacy's caster doesn't choose the order of the revealed cards

**Found:** 2026-11-27, rules-fixes run MC-1 (refactors-to-do.md entry 18, line O81); the owner chose the prompt 2026-11-28 and
moved it here the same day

**Problem.** Sword-Point Diplomacy reveals three cards, and for each one any opponent may pay 3 life to stop you from putting it
into your hand. The engine processes the revealed cards in library order. Its ruling: "In a multiplayer game, each opponent in
turn order chooses whether to pay life for one card before proceeding to the next card. You choose the order to perform this
process for the cards". With one opponent, who sees every revealed card, the order changes no outcome.

**Needs.** A caster prompt, card by card (the owner, 2026-11-28): the caster picks the next revealed card, each opponent in turn
order chooses whether to pay life for it, then the caster picks the next. No player gets priority and no hidden information
changes between the picks, so choosing the whole order up front would be equivalent. Test: in a three-player game, the caster's
chosen order decides which card the second opponent can still afford to pay for.

## 7. Nowhere to Run lifts hexproof only for its controller's spells and abilities

**Found:** 2026-11-27, rules-fixes run 0 (refactors-to-do.md entry 18, line T13); the owner decided the reading 2026-11-28, and it
moved here the same day

**Problem.** Nowhere to Run: "Creatures your opponents control can be the targets of spells and abilities as though they didn't
have hexproof." Unlike Glaring Spotlight it doesn't say "you control", and the owner decided (2026-11-28) that it covers every
player's spells and abilities. The engine models it as `IgnoreOpponentHexproofEffect.opponentsCreatures()`, which
`GameQueryService.hasHexproofIgnore` reads for the targeting player only. With two players the outcome is the same: hexproof
only stops the creature controller's opponents (CR 702.11b), and the only one is Nowhere to Run's controller.

**Needs.** A breadth on `IgnoreOpponentHexproofEffect` for "spells and abilities" of any player: the hexproof of a creature is
lifted for every targeting player when a player of whom the creature's controller is an opponent has the effect. Test: in a
three-player game, the third player can target a hexproof creature of Nowhere to Run's controller's opponent.
