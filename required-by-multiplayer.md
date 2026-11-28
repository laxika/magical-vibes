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
