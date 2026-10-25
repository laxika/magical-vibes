# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 43. Static abilities of an instant or sorcery while it's on the stack (Torrent of Lava)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TorrentOfLavaTest` "A creature can prevent one damage from Torrent of Lava while it is on the stack"
- `TorrentOfLavaTest` "The caster's creature can prevent damage, without protecting another creature"
- `TorrentOfLavaTest` "The granted ability prevents only one damage"

**What the rules require.** CR 113.6: abilities of an instant or sorcery spell function while that
object is on the stack. Torrent of Lava says "As long as Torrent of Lava is on the stack, each
creature has '{T}: Prevent the next 1 damage that Torrent of Lava would deal to this creature this
turn.'" The card registers that grant as an `EffectSlot.STATIC` effect, but the layer system only
collects static effects from permanents (plus the graveyard/scheme wrappers), so the granted
activated ability never exists while the spell is on the stack.

**Why it's large.** It needs a new "stack static" source in `LayerSystemService.collectInstances`
(similar to `GraveyardStaticEffect`/`WrappedGraveyardStaticEffect`), layer classification for it,
invalidation when the spell enters or leaves the stack, and a prevention shield keyed to the spell
as the damage source.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 43: let an instant or sorcery's static abilities apply
while it is on the stack (CR 113.6), so Torrent of Lava grants each creature "{T}: Prevent the next
1 damage that Torrent of Lava would deal to this creature this turn" only while it is on the stack.
Add a stack-static collection path to LayerSystemService alongside the graveyard static wrappers.
Make the three failing TorrentOfLavaTest cases pass. Verify every CR number with the rules MCP.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 44. Ordering competing mill replacement effects (The Water Crystal + Bruvac)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TheWaterCrystalTest.affectedOpponentChoosesOrderOfMillReplacementEffects()`

**What the rules require.** CR 616.1: when two or more replacement effects try to modify the same
event, the affected player chooses one to apply, then repeats for the rest. The Water Crystal ("If
an opponent would mill one or more cards, they mill that many cards plus four instead") and Bruvac
the Grandiloquent (doubling) give different totals depending on order, so the milling opponent must
choose. `GraveyardService.resolveMillPlayer` applies them in a fixed order with no prompt.

**Why it's large.** Mill isn't routed through `ReplacementOrderService`. It needs a mill event
object, candidate collection for every mill-modifying replacement, a resumable completion after the
choice, and migration of every mill caller onto the new path.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 44: route mill events through replacement ordering
(CR 616.1) so the affected player chooses the order of competing mill replacement effects such as
The Water Crystal and Bruvac the Grandiloquent. Add a mill event plus a resumable completion in
GraveyardService.resolveMillPlayer and reuse ReplacementOrderService. Make
TheWaterCrystalTest.affectedOpponentChoosesOrderOfMillReplacementEffects pass. Verify every CR
number with the rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 45. Optional additional votes from a static ability for every vote (Tivit)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TivitSellerOfSecretsTest.additionalVoteAppliesToAnotherCardsVote()`

**What the rules require.** CR 701.38a: to vote, each player chooses one of the options, starting
with a specified player and proceeding in turn order. Tivit, Seller of Secrets says "While voting,
you may vote an additional time", which applies to every vote its controller takes part in (Plea
for Power in the test), not only Tivit's own council's dilemma. The engine supports the extra vote
only inside `CouncilDilemmaEffectHandler`; `VotingSupport.countAdditionalVotes` counts only The
Valeyard's mandatory extra vote and never asks.

**Why it's large.** About 17 vote handlers each run their own voting loop. A generic optional extra
vote needs a static marker effect, a shared "vote again?" prompt in `VotingSupport`, and every vote
handler moved onto that shared loop.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 45: make "While voting, you may vote an additional time"
(Tivit, Seller of Secrets) work for every vote, not only council's dilemma. Add a static marker
effect and an optional extra-vote prompt to VotingSupport, and move the vote handlers onto the
shared voting loop. Make TivitSellerOfSecretsTest.additionalVoteAppliesToAnotherCardsVote pass and
keep The Valeyard's mandatory extra vote working. Verify every CR number with the rules MCP. Don't
run the full test suite; ask me. Don't commit until I say so.
```

---

## 46. Choosing which coin flip to ignore (Krark's Thumb)

**Status:** open · **Found:** 2026-10-22, while fixing T-card test failures

**Failing tests:**
- `TwoHeadedGiantTest.krarksThumbLetsControllerChooseWhichResultsToIgnore()`

**What the rules require.** Krark's Thumb: "If you would flip a coin, instead flip two coins and
ignore one." The controller sees both results and chooses which one to ignore. CR 705.3 covers
effects that dictate a flip's result. The engine's `CoinFlipService` treats the pair as "heads if
either is heads", which is right only for flips the player wants to win and gives no choice for
effects such as Two-Headed Giant's two separate flips, where the controller may prefer tails.

**Why it's large.** Every coin-flip effect resolves its flips synchronously. A player choice per
replaced flip needs a resumable prompt in the middle of each flipping effect, across all
coin-flip handlers.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 46: make Krark's Thumb flip two coins and let its
controller choose which result to ignore, instead of CoinFlipService auto-preferring heads. Add a
resumable choice to the coin-flip path used by every coin-flip handler. Make
TwoHeadedGiantTest.krarksThumbLetsControllerChooseWhichResultsToIgnore pass. Verify every CR number
with the rules MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 47. Per-slot target legality for "up to one target X, up to one target Y" spells

**Status:** open · **Found:** 2026-10-22, while fixing U-card test failures

**Failing tests:**
- `UrgentNecropsyTest.artifactTargetThatLosesArtifactTypeIsNotDestroyedAsACreature()`

**What the rules require.** Urgent Necropsy: "Destroy up to one target artifact, up to one target
creature, up to one target enchantment, and up to one target planeswalker." CR 608.2b says each
target is checked against the instance of the word "target" it was chosen for. The Plague Spores
example in that rule is this exact case: a target chosen only as "the artifact" that stops being an
artifact is illegal and must not be destroyed, even if it is now a creature. The card uses a single
`target(AnyOf, 0, 4)` group plus the
`AT_MOST_ONE_ARTIFACT_ONE_CREATURE_ONE_ENCHANTMENT_AND_ONE_PLANESWALKER` constraint. At resolution
each target is rechecked only against the combined AnyOf filter, so the target stays legal.

**Why it's large.** Splitting the card into four optional `target(..., 0, 1)` groups doesn't work.
Casting passes one flat `targetIds` list, and mapping it onto optional groups is positional. The
engine would need an explicit per-slot target assignment carried from casting to the stack entry,
across `TargetLegalityService`, `ValidTargetService`, the cast paths, the harness `cast*` overloads
and the AI target selector. `ChaoticTransformation`, `BoomBox`, `FinaleOfPromise` and
`ShredsOfSanity` use the same constraint approach and share the bug.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 47: record which slot ("target artifact", "target
creature", ...) each target of a constraint-based multi-slot spell was chosen for, and recheck each
target against its own slot at resolution (CR 608.2b). Start with Urgent Necropsy and make
UrgentNecropsyTest.artifactTargetThatLosesArtifactTypeIsNotDestroyedAsACreature pass while keeping
the existing "at most one creature" rejection test. Then apply it to Chaotic Transformation, Boom
Box, Finale of Promise and Shreds of Sanity. Verify every CR number with the rules MCP. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 48. Declining an optional player target group in a multi-group trigger

**Status:** open · **Found:** 2026-10-22, while fixing V-card test failures

**Failing tests:**
- `VolatileArsonistTest.canDeclineEveryTargetGroup(boolean)` (both parameterizations)

**What the rules require.** Volatile Arsonist: "Whenever this creature attacks, it deals 1 damage to
up to one target creature, up to one target player, and up to one target planeswalker." Each "up to
one" lets the controller choose zero targets for that instance of "target" (CR 601.2c, applied to
triggered abilities by CR 603.3d), so declining all three groups must be possible. The controller
may also choose themselves for the player group, which `canChooseControllerAsPlayerTarget` covers.

**Why it's large.** The trigger target prompts (`ETBTokenTargetService.processNextETBTokenMultiTargetTrigger`
and `PermanentChoiceTriggerHandlerService.handleETBTokenMultiTargetTrigger`) use a single-select
`PermanentChoice` and treat "choose your own player id" as "stop this group" only when the
controller is not a legal target for it. For a player group that allows any player, the same answer
means "target myself", so a single-slot optional player group can't be declined at all. Fixing it
needs a distinct decline input, for example routing optional single-slot groups through
`beginMultiPermanentOrPlayerChoice` (an empty selection declines). That changes the prompt type for
every multi-group trigger card and the tests and AI code that answer those prompts.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 48: give optional ("up to one") target groups of
multi-group triggered abilities a decline answer that is distinct from choosing the controller as a
player target. Start with Volatile Arsonist: make
VolatileArsonistTest.canDeclineEveryTargetGroup pass while keeping
canChooseControllerAsPlayerTarget passing. Find the other cards that go through
ETBTokenTargetService / PermanentChoiceTriggerHandlerService multi-target triggers and update their
tests and the AI target selection to the new prompt. Verify every CR number with the rules MCP.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 49. Playing cards from the top of another player's library

**Status:** open · **Found:** 2026-10-22, while fixing W-card test failures

**Failing tests:**
- `WindriddlePalacesTest.offersBothPlayersTopCardsWhenBothLibrariesAreNonempty()`

**What the rules require.** Windriddle Palaces: "You may play lands and cast spells from the top of
any player's library." Each player may play the top card of every library, not only their own. The
engine assumes a single library: `CastingPermissionService.findLibraryTopOwner` returns one owner,
the view's `playableLibraryTopCards` lists one card, and `PlayCardRequest` carries no library owner,
so a cast from the library top always uses that one library.

**Why it's large.** It is a protocol change across the stack: a list of library owners in the
playable-top-card view and in `canPlayLandFromTopOfLibrary`; a new `libraryOwnerId` field on
`PlayCardRequest` (many constructor overloads) threaded through `PlayCardRequestDispatchService` into
`SpellCastingService.playCardFromLibraryTop`; and frontend changes (`game.component.ts`,
`targeting-choice.service.ts`) so the UI sends which library the card comes from. Only showing both
cards in the view would let the UI offer the opponent's card but cast the player's own.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 49: let a player play cards from the top of any player's
library when an effect allows it (Windriddle Palaces). Make the playable-library-top view list every
eligible library's top card with its owner, add a library-owner id to PlayCardRequest and thread it
through PlayCardRequestDispatchService into SpellCastingService.playCardFromLibraryTop and the land
path, and update the Angular client to send it. Make
WindriddlePalacesTest.offersBothPlayersTopCardsWhenBothLibrariesAreNonempty pass and keep the other
play-from-top-of-library card tests (e.g. Future Sight, Courser of Kruphix, Bolas's Citadel) passing.
Verify every CR number with the rules MCP. Don't run the full test suite; ask me. Don't commit until
I say so.
```

---

## 51. Aura and Equipment restrictions modelled as abilities granted to the creature

**Status:** open · **Found:** 2026-10-23, auditing `GrantEffectEffect` attached scopes after the Gutter Shortcut fix

**Failing tests:** none known; the bug shows only when the creature loses its abilities (Dress Down, Humility,
Turn to Frog) or when "has" text is wrongly kept.

**What the rules require.** "Enchanted creature can't ..." / "Equipped creature can't ..." is a static ability of the
Aura or Equipment itself; removing the creature's abilities (layer 6, CR 613.1f) can't touch it. Only "has"/"gains"
text grants an ability to the creature. Wrapping the effect in `GrantEffectEffect(..., ENCHANTED_CREATURE /
EQUIPPED_CREATURE / ENCHANTED_PERMANENT)` models a granted ability. Gutter Shortcut was fixed (effect directly in
STATIC; `BlockLegalityService` now reads attached restrictions even after ability loss). Remaining cases:
- Card-only fixes (the engine already reads the effect from an attached permanent — `BlockLegalityService`
  restriction kinds and `GameQueryService.hasCantBeBlocked`/`hasAuraWithEffect`, which unwraps `ConditionalEffect`
  and `EnchantedPermanentConditionalEffect`):
  - `SecurityBypass` (SNC 59) — "As long as enchanted creature is attacking alone, it can't be blocked." Keep the
    granted connive trigger, which is a quoted ability.
  - `WayOfTheThief` (GTC 56) — "Enchanted creature can't be blocked as long as you control a Gate." Use
    `ConditionalEffect(cond, new CantBeBlockedEffect())`.
  - `ThievesTools` (AFR 122) — "Equipped creature can't be blocked as long as its power is 3 or less." Likely
    `EnchantedPermanentConditionalEffect(PermanentPowerAtMostPredicate(3), new CantBeBlockedEffect(), null)`; verify
    it works for Equipment.
  - `PsychicPaper` (WHO 181) and `BrotherhoodRegalia` (ACR 71) — "Equipped creature has ward ... and can't be
    blocked": only the ward is granted; "can't be blocked" is the Equipment's own restriction.
- Engine changes needed (these restriction kinds are only read from the creature's own or granted effects, see
  `GameQueryService` and `AuraAttachmentService`):
  - `TargetingRestrictionEffect` — `ArtifactWard` (ATQ 3, "can't be the target of abilities from artifact sources"),
    `AntiMagicAura` (5ED 72, "can't be the target of spells"), `SpectralShield` (ICE 302, "can't be the target of
    spells").
  - `CantBeEnchantedByOtherAurasEffect` — `AntiMagicAura`, `ConsecrateLand` (2ED 15; its indestructible is correctly
    granted).
  - `CantBeSacrificedEffect` — `AssaultSuit` (CMM 369).
  - `CantBecomeSuspectedEffect` — `AirtightAlibi` (MKM 149).
- The reverse mistake (granted keyword modelled as the attached permanent's own effect, so it survives ability
  loss): `TravelersCloak` (INV 83, "Enchanted creature has landwalk of the chosen type") and `VectisGloves` (MH2 241,
  "has artifact landwalk") put `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` directly in STATIC. Landwalk
  is a keyword (CR 702.14c); wrap it in `GrantEffectEffect` like `TrailblazersBoots`.

**Why it's large.** Four restriction kinds need their attached-permanent lookups added in `GameQueryService`
(targeting legality is heavily shared), plus eleven card changes, each with a "loses all abilities" test.

**Prompt to start it:**

```
Fix the Aura/Equipment restriction modelling in missing-features-to-do.md entry 51. First the card-only fixes
(Security Bypass, Way of the Thief, Thieves' Tools, Psychic Paper, Brotherhood Regalia, Traveler's Cloak, Vectis
Gloves), then make GameQueryService/AuraAttachmentService read TargetingRestrictionEffect,
CantBeEnchantedByOtherAurasEffect, CantBeSacrificedEffect and CantBecomeSuspectedEffect from attached Auras and
Equipment, and convert Artifact Ward, Anti-Magic Aura, Spectral Shield, Consecrate Land, Assault Suit and Airtight
Alibi. Verify every Oracle text, and add a Dress Down (loses all abilities) test per card.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 52. "Loses this ability": client listing and copy effects

**Status:** open · **Found:** 2026-10-23, after adding `LoseThisActivatedAbilityEffect` for Great Fang Chroniclers

**Failing tests:** none yet.

**What the rules require.** After "then this creature loses this ability" the permanent no longer has that
activated ability. The engine records it by printed-ability index in `Permanent.lostPrintedActivatedAbilityIndices`,
and activation and `PredicateEvaluationService.hasAnyPrintedAbility` honour it. Two gaps remain:
- `PermanentViewFactory` strips activated abilities only for `losesAllAbilities`/`losesAllNonManaAbilities`, so the
  client (and anything listing abilities from the view) still offers the lost ability until activation is refused.
- The record is an index into the card's printed abilities. If the permanent later becomes a copy of something else
  or changes face, the index would hide the new object's ability at that position. Only the removed ability itself
  should stay gone, so the record should be tied to the ability, or cleared or reinterpreted when the copiable
  values change. Not verified with a test yet.

**Why it's large.** Small, but it touches the view layer and copy and transform handling.

**Prompt to start it:**

```
Finish "loses this ability" (missing-features-to-do.md entry 52): hide lost printed activated abilities in
PermanentViewFactory, and make the lost-ability record survive copy and transform correctly. Write tests first:
Great Fang Chroniclers after using its ability, then becoming a copy of a creature with an activated ability.
Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 53. Ordering token-creation replacements outside the single-blueprint creation path

**Status:** open · **Found:** 2026-10-25, while finishing entry 35 (token replacement ordering)

**Failing tests:**
- None yet. Write one per path, e.g. Queen Allenal of Ruadach + Parallel Lives with a token copy
  (Cackling Counterpart) or a token created through `DestructionSupport.createTokenForPlayer`: the player
  must be asked, and Queen first must give the doubled Soldier.

**What the rules require.** CR 616.1 / 616.1e: the player creating the tokens chooses the order of every
replacement that modifies the token creation event. `PermanentControlSupport.applyCreateToken` (the
single-blueprint path) now runs a `TokenCreationEvent` where count replacements (`TokenCreationReplacements`)
and the Soldier/Frog/Squirrel additions (`TokenAdditionReplacements`) are ordered. Still fixed, with no prompt:
- `DestructionSupport.createTokenForPlayer`, `TokenCopySupport` and the copy handlers that call
  `getTokenCreationAmount` (multiplier first, additions unmultiplied).
- In the single-blueprint path, the Map, Thopter, Mutagen, Food (Tippy-Toe), Treasure (Bilbo), Academy
  Manufactor and solved-Case Clue additions/replacements are applied after the ordered event, so a doubler
  never doubles them and Chatterfang never counts them (before entry 35 Chatterfang counted them).

**Why it's large.** Each remaining site creates tokens mid-effect and would need to suspend on the order
prompt and resume (as `ChoiceHandlerService` does for `TokenCreationEvent`). Academy Manufactor replaces the
token kind, so it needs a non-count option on the event.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 53: route the remaining token creation paths
(DestructionSupport.createTokenForPlayer, TokenCopySupport, the token copy handlers) and the remaining
additions (Map, Thopter, Mutagen, Tippy-Toe Food, Bilbo Treasure, Academy Manufactor, solved Case Clues)
through TokenCreationEvent and ReplacementOrderService, as TokenAdditionReplacements does for Queen
Allenal, Quina and Chatterfang. Write the tests first. Don't run the full test suite; ask me. Don't
commit until I say so.
```

---

## 54. Nafs Asp's pay-before-draw special action has no client action, and the AI never pays

**Status:** open · **Found:** 2026-10-25, while finishing entry 36 (Quenchable Fire's special action)

**Failing tests:**
- None yet. Write one: a human player damaged by Nafs Asp (or Glass Asp) is offered the payment in
  `GameStateMessage.delayedTriggerPayments` while holding priority before their draw step, auto-pass stops
  for it, and paying clears `priorityPassedBy`.

**What the rules require.** CR 116.2c: paying "unless they pay {1} before that draw step" is a special action
the player may take any time they have priority until the step begins; CR 117.4: an earlier pass no longer
counts as passing in succession after an action. Entry 36 added the generic delayed-trigger payment
(`GameActionAvailabilityService.getDelayedTriggerPaymentsOwedBy`, `AbilityActivationService.payDelayedTriggerCost`,
`PAY_DELAYED_TRIGGER_COST`, a side-panel button and an auto-pass stop) for Quenchable Fire and Sabertooth
Cobra. Nafs Asp and Glass Asp (`LoseLifeAtNextDrawStepUnlessPays`) still pay only through
`GameService.payDrawStepLifeLoss`, which no client message reaches, auto-pass doesn't stop for, and which
keeps `priorityPassedBy` (`AbilityActivationServiceTest.drawStepPaymentRemovesOnlyOneIdenticalObligation`
asserts that). The AI never makes any of these payments.

**Why it's large.** Moving the Asps onto the generic payment changes their payment path and the service
tests that pin it; teaching the AI when paying is worth it needs a planner hook for special actions.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 54: add LoseLifeAtNextDrawStepUnlessPays (Nafs Asp, Glass Asp)
to GameActionAvailabilityService.getDelayedTriggerPaymentsOwedBy so it goes through
AbilityActivationService.payDelayedTriggerCost and the PAY_DELAYED_TRIGGER_COST client action, retire
payDrawStepLifeLoss, and let the AI pay delayed-trigger costs when it is worth it. Don't run the full test
suite; ask me. Don't commit until I say so.
```

---

## 55. Other "until end of turn" stores under Runed Terror's sequential phases

**Status:** open · **Found:** 2026-10-25, while finishing entry 38 (Runed Terror durations)

**Failing tests:**
- None yet. Write one per store moved, in the style of `RunedTerrorTest.temporaryBoostSurvivesOtherPlayersCleanup`
  (e.g. player 2 casts a "can't block this turn" or damage-prevention spell during their phase; it must still
  apply after player 1's cleanup step and end at player 2's).

**What the rules require.** Runed Terror ruling (2024-11-08): "until end of turn" and "this turn" effects end
during the cleanup step of the player who was active when the effect began (CR 514.2 ends them at cleanup).
Entry 38 made this hold for floating continuous effects (`floatingEffectCreatingActivePlayerIds`,
`GameData.expireEndOfTurnFloatingEffectsCreatedDuring`) and for `Permanent` power/toughness modifiers and
granted keywords (`GameData.phaseSequenceModifierShares`, `TemporaryModifierShare`). Every other store that
`TurnCleanupService.resetEndOfTurnModifiers` / `Permanent.resetModifiers` clears (the other per-permanent
flags such as `cantBlockThisTurn`, `removedKeywords`, protections, transient types/colors, base P/T
overrides, damage-prevention and redirection shields, "this turn" player sets and cast/play permissions)
still ends at whichever player's cleanup comes first. A keyword granted by the active player and again by
another player (same keyword on a `Permanent.grantedKeywords` set) is credited only to the first granter.

**Why it's large.** Each store needs the creating active player recorded, or a per-player split like
`TemporaryModifierShare`, and roughly two hundred stores are involved.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 55: extend entry 38's Runed Terror duration handling
(TurnCleanupService.attributeTemporaryModifiersToActivePlayer / phaseSequenceModifierShares and the
floating-effect tagging) to the remaining "until end of turn" / "this turn" stores, so each ends only at the
cleanup step of the player who was active when it began (Runed Terror ruling; CR 514.2). Start with the
most-used ones (Permanent.resetModifiers flags, damage-prevention shields) and add RunedTerrorTest cases for
each. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 56. A standalone "any colour" mana activation still holds its non-mana triggers

**Status:** open · **Found:** 2026-10-25, while finishing entry 39 (mana-ability trigger timing)

**Failing tests:**
- None yet. Write one: activate Birds of Paradise (or a land whose mana needs a colour prompt) with no
  payment intent while a non-mana trigger watches it, answer the colour prompt, and expect the trigger on
  the stack.

**What the rules require.** CR 603.3: a triggered ability goes on the stack the next time a player would
receive priority. A mana ability activated with no payment in progress resolves, and its controller then
receives priority (CR 117.3c), so its non-mana triggers belong on the stack then. Since entry 39,
`GameService.finishPriorityManaActivation` does this for activations that finish at once. An activation that
stops for a colour choice parks in `GameData.pendingRevertableManaActivation`. The answer handlers then
complete it into a revertable `ManaActivation`, and `GameService.handleInteractionAnswer` still uses the
old flush, which waits while any revertable activation exists. So with no payment intent, its triggers
wait until the next priority pass or cast.

**Why it's large.** The payment intent is only known on the `tapPermanent`/`activateAbility` call. To
carry it across the colour prompt, `PendingManaActivation` (or a sibling field) has to record "standalone",
and every `completeParkedManaActivation` call site in `ChoiceHandlerService` (about ten) has to commit
instead of recording a revertable activation. Then the answer path needs the standalone flush.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 56: when a mana ability activated with no ManaPaymentIntent
parks for a colour choice (pendingRevertableManaActivation), make its non-mana triggers go on the stack once
the colour is chosen (CR 603.3, CR 117.3c), and make that activation non-revertable, matching what
GameService.finishPriorityManaActivation already does for activations that finish at once. Keep
intent-tagged (payment) activations held and revertable. Add tests next to ManaActivationRevertTest. Don't
run the full test suite; ask me. Don't commit until I say so.
```

---

## 57. Per-unit copy riders for Pyromancer's Goggles and Primal Wellspring

**Status:** open · **Found:** 2026-10-25, while finishing entry 40 (Sunken Palace's per-unit copy rider)

**Failing tests:**
- `PyromancersGogglesTest` "Each mana produced with a mana multiplier grants its own copy"
- `PyromancersGogglesTest` "Spending Goggles mana on a non-red spell cannot copy a later red spell"
- `PyromancersGogglesTest` "Only Goggles mana actually spent on a spell creates copies"

**What the rules require.** CR 106.6 / 106.6a: "When that mana is spent to cast a red instant or
sorcery spell, copy that spell" is a delayed trigger on the mana itself, one per unit produced. The
engine still arms these per player (`GameData.pendingNextRedInstantSorceryCopyCount`,
`pendingNextInstantSorceryCopyCount`, set in `AnyColorManaChoiceSupport`), so the next matching spell is
copied whatever mana paid for it. Sunken Palace now uses a pool tag instead
(`ManaPool.addCopyGrantingMana` / `takeSpentCopyGrantingMana`, claimed in
`TriggerCollectionService.checkSpellCastTriggers`, discarded at the end of every action by
`GameData.discardUnclaimedSpentManaRiders`). Goggles and Wellspring need the same tag, with the spell
filter (red instant/sorcery, instant/sorcery) checked when the spent units are claimed: units spent on a
spell that doesn't match are spent without effect. Also: Sunken Palace's mana that is added only after a
prompt (a land-mana replacement order or color choice) is not tagged yet (`tagCopyGrantingMana` in
`ActivatedAbilityExecutionService` runs only when no mana choice is pending).

**Why it's large.** Medium: two more tags through every `ManaPool` copy/merge/convert/drain path, and the
any-color choice handlers must tag the chosen color.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 57: replace the per-player pendingNextRedInstantSorceryCopyCount /
pendingNextInstantSorceryCopyCount arming for Pyromancer's Goggles and Primal Wellspring with per-unit
ManaPool tags like Sunken Palace's copyGrantingMana (CR 106.6a), so only the matching spell the tagged
mana actually pays for is copied, once per unit. Make the failing PyromancersGogglesTest cases pass and
add Primal Wellspring tests. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 58. Opponent-chosen-target spells cast from another player's hand (Sen Triplets)

**Status:** open · **Found:** 2026-10-25, while finishing entry 41 (casting from another player's hand)

**Failing tests:** none yet. Casting such a spell from another player's hand is refused with an
`IllegalStateException`.

**What the rules require.** Sen Triplets' rulings say a card played from that player's hand is played
"just like playing any other card", so a spell whose targets an opponent chooses (a
`GainControlOfTargetEffect` with `opponentChoosesTarget`, or `Card.getOpponentChosenSpellTargetIndices`)
can be cast from it. The engine stops the cast for the opponent's choice
(`PermanentChoiceContext.OpponentChosenSpellTarget(s)`) and resumes it with
`SpellCastingService.playCardAfterOpponentChosenTarget(s)`, which indexes the caster's own hand, so it
would cast the wrong card. `SpellCastingService.rejectOpponentChosenTargetsFromAnotherHand` refuses the
cast instead. Also open: the hard AI's MCTS search (`GameSimulator`) doesn't list plays from another
player's hand; only the base `AiDecisionEngine.tryPlayFromOpponentHand` heuristic plays them.

**Why it's large.** Small to medium: the two prompt contexts need the hand owner, and their resume path
has to run inside `SpellCastingService.playFromHandOf` so the owner gets stamped.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 58: let a spell whose targets an opponent chooses be cast from
another player's hand under Sen Triplets. Carry the hand owner in PermanentChoiceContext.OpponentChosenSpellTarget
and OpponentChosenSpellTargets, resume the cast through SpellCastingService.playFromHandOf, and remove
rejectOpponentChosenTargetsFromAnotherHand. Optionally add foreign-hand plays to GameSimulator's action list.
Add SenTripletsTest coverage. Don't run the full test suite; ask me. Don't commit until I say so.
```

---

## 59. Split-card names in "choose a card name" prompts and chosen-name checks

**Status:** open · **Found:** 2026-10-25, while finishing entry 42 (aftermath cards as split cards)

**Failing tests:** none yet.

**What the rules require.** "Each split card has two names. If an effect instructs a player to choose a
card name and the player wants to choose a split card's name, the player must choose one of those names
and not both. An object has the chosen name if one of its names is the chosen name" (CR 709.4a).
`PlayerInputService.collectCardNamesInGameExcluding` offers each split card's whole name ("Commit //
Memory") as well as its halves, so a player can choose both names at once. Several chosen-name checks
still compare `getName()` to the chosen name instead of using `Card.hasName`, so they miss a split card
(or a Room) named by one half: `GameQueryService` (player protection from the chosen name, damage from
a source with the chosen name, activated abilities of the chosen name),
`AbilityActivationService.validateNotBlockedByNameLock`, `CastingPermissionService.isLandPlayForbiddenByChosenName`,
`IncreaseCastCostForChosenNameSpellsEffectHandler` and `TargetLegalityService`'s
`StackEntrySharesChosenNameWithSourcePredicate`. Entry 42 switched the card-in-zone checks (Conundrum
Sphinx, Mindblaze, Liar's Pendulum, Booby Trap, `CardHasSourceChosenNamePredicate`) to `hasName`.

**Why it's large.** Small to medium. The string-based checks take a name, not a card, so their callers
need to pass the object; and dropping the whole name from the prompt may break tests that choose it.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 59 (CR 709.4a): stop offering a split card's whole name in
"choose a card name" prompts (PlayerInputService.collectCardNamesInGameExcluding and the catalog name
list), and make the remaining chosen-name checks listed in the entry match either half's name through
Card.hasName. Add card tests with an aftermath or split card (e.g. Commit // Memory, Fire // Ice) for
each touched effect. Verify every CR number with the rules MCP. Don't run the full test suite; ask me.
Don't commit until I say so.
```

---

<!--
Template — copy everything between the lines, give it the next number, and append above.

## N. <Short title>

**Status:** open · **Found:** YYYY-MM-DD, <context>

**Failing tests:**
- `<TestClass>.<test>()` (or the display name)

**What the rules require.** <Verified CR text / rulings, and what the engine does instead.>

**Why it's large.** <Scope and regression risk.>

**Prompt to start it:**

```
<A self-contained prompt that can be pasted to kick off the feature.>
```
-->
