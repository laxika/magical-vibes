# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 63. Declining "up to one target player" in single-target and graveyard-mixed trigger prompts

**Status:** open · **Found:** 2026-10-26, while finishing entry 48 (declining optional player groups)

**Failing tests:**
- None yet; found by reading the code. Write a test first (a single-group "up to one target player"
  trigger whose controller declines).

**What the rules require.** "Up to one target" lets the controller choose zero targets for that
instance of "target" (CR 601.2c, applied to triggered abilities by CR 603.3d). Entry 48 gave the
slot-by-slot walker (`ETBTokenTargetService.processNextETBTokenMultiTargetTrigger`) an empty-answer
decline for optional groups that can target the controller. Two prompts still treat "choose
yourself" as both "decline" and "target myself": the single-target `ETBTokenTargetTrigger`
(`ETBTokenTargetService.controllerChoiceDeclines` only declines when the controller is not a legal
target, so an optional "up to one target player" trigger can't be declined), and walker groups that
mix player targets with graveyard or exiled-card candidates (they skip the `EtbPlayerTargetGroup`
prompt).

**Why it's large.** The single-target prompt is a `PermanentChoice` shared by many trigger types
and the AI; it needs a distinct decline answer (or routing through a multi-choice prompt) and the
tests and AI that answer it must follow.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 63: let the controller decline an optional ("up to one")
trigger target that they could also legally target themselves, in the single-target
ETBTokenTargetTrigger prompt and in walker groups that mix players with graveyard/exiled cards.
Find a card with a single "up to one target player" trigger, write a failing decline test, then
give the prompt a decline answer distinct from choosing the controller (see how entry 48 used
MultiPermanentChoiceContext.EtbPlayerTargetGroup). Update the AI and harness answering code.
Verify every CR number with the rules MCP. Don't run the full test suite; ask me. Don't commit
until I say so.
```

---

## 64. Own library top hidden while a temporary grant opens another player's library

**Status:** open · **Found:** 2026-10-26, while finishing entry 49 (playing from any player's library)

**Failing tests:**
- none yet (write one: Future Sight plus Xanathar, Guild Kingpin's grant on the opponent's library)

**What the rules require.** Each permission works on its own library: Future Sight still lets its
controller play the top card of their own library while Xanathar lets them play the top card of the
target opponent's library, so both cards should be playable. `CastingPermissionService.findLibraryTopOwners`
only adds the player's own library when no temporary grant opens a nonempty library (and no
any-library permission applies), so the own top card is not offered. Its top-library permission checks
(`canCastFromTopOfLibrary`, `canCastFromTopOfLibraryNormally`, `hasAnyLibraryTopPermission`, the free
and life play permissions) are not library-aware either, so simply adding the own library could let a
permission meant for one library authorize the other's card.

**Why it's large.** Every top-library permission check needs a library owner (own-library sources like
Future Sight, Courser of Kruphix, Bolas's Citadel vs. grants on a specific library), through the
projection, the cast and land paths, and the AI's playability query.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 64: make the top-library permission checks in
CastingPermissionService library-aware (own-library permissions only for the player's own library,
grants only for their granted library, any-library permissions for all), then let
findLibraryTopOwners include the player's own library alongside temporarily granted ones. Add a test
with Future Sight plus Xanathar, Guild Kingpin showing both top cards are offered and playable, and
keep WindriddlePalacesTest, XanatharGuildKingpinTest, FutureSightTest, CourserOfKruphixTest and
BolassCitadelTest passing. Verify every CR number with the rules MCP. Don't run the full test suite;
ask me. Don't commit until I say so.
```

---

## 65. Indomitable Might's damage option and attached restrictions in the client listing

**Status:** open · **Found:** 2026-10-26, while finishing entry 51 (Aura/Equipment restrictions as the attachment's own abilities)

**Failing tests:**
- none yet (write one: Indomitable Might on a creature that loses all abilities to Dress Down, then blocked)

**What the rules require.** "Enchanted creature's controller may have it assign its combat damage as
though it weren't blocked" (Indomitable Might, AFC 40) is a static ability of the Aura, not an ability
granted to the creature (CR 113.10: only "has"/"gains" text adds an ability), so it keeps working after
the creature loses its abilities (CR 613.1f). The card wraps `AssignCombatDamageAsThoughUnblockedEffect`
in `GrantEffectEffect(..., ENCHANTED_CREATURE)`, and `CombatDamageService` only reads that effect from the
creature's own and granted effects. Separately, restrictions now modelled with
`AttachedPermanentRestrictionEffect` (Anti-Magic Aura, Artifact Ward, Spectral Shield, Consecrate Land,
Assault Suit, Airtight Alibi) are no longer granted effects, so `GrantedAbilityViewFactory` no longer
lists them on the enchanted/equipped permanent in the client.

**Why it's large.** Small engine change in `CombatDamageService` (read the effect from attached Auras,
honouring Volrath's Curse and the Aura losing its own abilities) plus a client-listing source for
attachment-imposed restrictions; both need tests.

**Prompt to start it:**

```
Implement missing-features-to-do.md entry 65. Make CombatDamageService read
AssignCombatDamageAsThoughUnblockedEffect from Auras attached to the attacker (like
GameQueryService.hasAuraWithEffect) and put Indomitable Might's effect directly in STATIC instead of a
GrantEffectEffect; add a Dress Down test. Then have the permanent view list restrictions that attached
Auras/Equipment impose through AttachedPermanentRestrictionEffect. Verify every CR number with the rules
MCP. Don't run the full test suite; ask me. Don't commit until I say so.
```

## 66. Activated-ability indices shift when abilities drop out of the effective list

**Status:** open · **Found:** 2026-10-26, while finishing entry 52 ("loses this ability" listing and copies)

**Failing tests:** none yet.

**What the rules require.** Each activated ability is its own ability; "activate only once each turn" and similar
limits track that ability. The engine keys per-turn and per-game activation counts
(`GameData.activatedAbilityUsesThisTurn`) by the position in `AbilityActivationService.getEffectiveActivatedAbilities`.
That list drops abilities the permanent lost ("loses this ability", losing all non-mana abilities), so a later
ability's position shifts mid-turn and it can inherit or lose another ability's count. Separately, `CardViewFactory`
leaves exile-only abilities out of the client's list while the engine list keeps them, so a client index after an
exile-only ability points one too far.

**Why it's large.** Activation limits, the AI and the client all use the position; keying counts by ability
identity touches every limit check.

**Prompt to start it:**

```
Fix missing-features-to-do.md entry 66: key activation-limit counts by ability rather than effective-list position,
and make the client ability list and the engine's effective list agree on exile-only abilities. Write tests first:
a permanent with a lost ability before a once-per-turn ability, activated before and after the loss.
Don't run the full test suite; ask me. Don't commit until I say so.
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
