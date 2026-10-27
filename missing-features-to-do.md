# Missing features to do

Engine features that don't exist yet, found while fixing card tests. Each entry lists the tests that
fail because the feature is missing, what the rules require, and a ready-to-use prompt that kicks off
the implementation.

To add an entry, copy the template at the bottom, fill it in, and append it above the template.
Remove an entry once the feature has landed and its tests pass.

---

## 66. Activated-ability indices shift when abilities drop out of the effective list

**Status:** partial · **Found:** 2026-10-26, while finishing entry 52 ("loses this ability" listing and copies)

**Failing tests:** none.

**Progress (2026-10-27).** Landed: `AbilityActivationService.perTurnUseKey` now keys per-turn and per-game
activation counts by the ability, not its effective-list position. A printed ability is keyed by its index
among the card's printed abilities (what `ActivationCount` names; transformed faces still offset by 1000),
so losing an earlier ability ("loses this ability", losing all abilities) no longer moves a later
ability's count. `getEffectiveActivatedAbilities` also drops exile-only abilities (CR 113.6b) unless the
card is in exile, so engine indices match the client's battlefield ability list. Tests:
`ActivationLimitAbilityIdentityTest` (application module).

**What remains.** Abilities the card doesn't print (static grants, persistent/temporary/until-next-turn
abilities) are keyed by their position among the permanent's non-printed abilities (offset 2000). That
is stable when printed abilities are lost, but if an earlier granted ability goes away mid-turn (an
Equipment moves, a grant ends), a later granted once-per-turn ability still shifts onto the earlier
one's count. Static grants are rebuilt as copies on each layer pass, so identity can't key them; a fix
needs a stable grant identity (granting source + ability within the grant, like `acquisitionId`).

**Prompt to start it:**

```
Finish missing-features-to-do.md entry 66: give non-printed activated abilities (static grants, temporary
and persistent grants) a stable activation-limit key so a once-per-turn granted ability keeps its own count
when an earlier granted ability goes away mid-turn. Write tests first: a creature with two granted
once-per-turn abilities from two Equipment, activate the second, unattach the first, try again.
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
