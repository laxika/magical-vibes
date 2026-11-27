# Rules fixes: the effect cleanup's open defects

A step-by-step plan for the 252 open defect lines in `refactors-to-do.md` entry 18 ("Rules defects seen during the effect
cleanup"). The lines are split into **runs**: one subagent each. Runs are grouped into **tracks** by the engine area they change.
Runs in one track go one after the other. Runs in different tracks go in parallel, a wave at a time, each agent in its own
worktree, and the orchestrator merges them.

To start, use the orchestrator prompt at the bottom. The brief for one agent is under "Agent brief".

## Line IDs

Each line has an ID: a group letter and its position in that group's list in entry 18 **as of commit `0e1a0e686aa`**. The IDs
stay valid after lines are deleted from entry 18, because they always refer to that commit.

| Letter | Entry 18 group |
|---|---|
| F | Face-down and lost-ability reads (12) |
| M | Two-player assumptions (`getOpponentId`, first seat) (17) |
| S | Simultaneity and APNAP (23) |
| E | Missing events or records (16) |
| T | Wrong targets and choices (22) |
| P | Payments and costs (25) |
| L | Layers and copy values (19) |
| H | Logs and hidden information (10) |
| O | Other (108) |

Print every line with its ID:

```
git show 0e1a0e686aa:refactors-to-do.md | awk 'BEGIN{split("F M S E T P L H O",k," ")} /^## 18\./{on=1} on&&/^---/{on=0} on&&/^#### /{g++; n=0} on&&g&&/^- /{n++; print k[g] n ": " substr($0,3)}'
```

Pipe it to `/usr/bin/grep -E '^(S1|S9|E5):'` for a run's lines. A few lines in entry 18 were cut off mid-sentence (L1 Wickerwing
Effigy, O63 Library of Leng, O66 Gix, O106 Sarulf). The full queue line, which says where the defect was seen and gives more
detail, is in `git show 3b0d60d42d4:effect-cleanup-queues.md` under "Defect queue". Grep it for the card or class name.

## Status

| Track | Runs | Status |
|---|---|---|
| 0 Owner and bookkeeping | 0 | moves done; owner questions asked after the last run |
| DM Damage | DM-1, DM-2, DM-3 | DM-1, DM-2 done 2026-11-27; DM-3 open |
| FC Free casts and casting from other zones | FC-1, FC-2, FC-3 | FC-1 done 2026-11-27; FC-2, FC-3 open |
| CP Casting permissions and cost readers | CP-1 | open |
| MP Multiplayer opponent choices | MP-1, MP-2 | MP-1 done 2026-11-27; MP-2 open |
| CT Counters | CT-1, CT-2 | CT-1 done 2026-11-27; CT-2 open |
| EB Entry and leave batches | EB-1, EB-2, EB-3 | open |
| PY Payments | PY-1, PY-2, PY-3 | open |
| TR Triggers, subjects and targeting | TR-1, TR-2, TR-3 | open |
| LS Library searches and reveals | LS-1, LS-2 | open |
| TK Tokens and copies | TK-1, TK-2 | open |
| FD Face-down and hidden reads | FD-1, FD-2 | open |
| LY Layers, protection, text changes, linked abilities | LY-1 to LY-5 | open |
| DR Draws | DR-1 | open |
| CB Combat | CB-1 | open |
| PP Perpetual changes | PP-1 | open |
| PL Each player chooses, then all act | PL-1 | open |
| MC Single cards | MC-1, MC-2 | open |
| AI Hard / easy AI | AI-1, AI-2 | AI-1 done 2026-11-27; AI-2 open |
| UI Frontend | UI-1 | open |
| LG, CL Logs and non-rules cleanup | LG-1, CL-1 | open |
| FT Missing features | FT-1, FT-2 | open |
| Z Close | Z | open |

## Ground rules for every run

- **Rules accuracy first** (AGENTS.md). Read the line, then confirm it **with a test that fails on the current code** before you
  fix anything. Many lines say "Unverified" or "Unsure". If the test passes on the current code, the line is not a defect:
  drop it, keep the test as a pin if it covers something untested, and give the reason in the report.
- **The line is a lead, not a spec.** Read the code it names to the end. Check Oracle text with the Scryfall MCP `get_card` and
  rulings with `get_rulings`. Verify every CR number with the `rules` MCP `get_rule`, **including the numbers in the line
  itself**: some were written from memory. A line the rules and rulings don't settle becomes an owner question. Leave the code
  as it is, and put the question in the report.
- **Read first**: `AGENTS.md`, `agent-docs/ARCHITECTURE.md`, then only the topic file under `agent-docs/architecture/` that its
  table names for your area (`resolution.md` for anything a handler does). For what already exists, grep
  `agent-docs/architecture/effect-building-blocks.md`, `EFFECTS_INDEX.md` and `ORACLE_TEXT_EFFECT_MAP.md`. Never read them whole.
- **Reuse over creation.** Prefer an existing subject, step result, `Condition`, `DynamicAmount` or shared service (the damage
  pool, `ResolutionCostPaymentService`, `payFreeCastCostIncreases`, the entry batch, the leave batch) over a new class. Never add
  a parameter that only one card sets. A new effect or predicate gets its `agent-docs/` rows.
- **Grep every reader** of a class or method before changing it: engine, `magical-vibes-ai`, networking, application tests,
  card tests, `agent-docs/`, `.claude/skills/`. A shared handler serves cards with different rulings: when two cards need
  different answers, fix with a per-card component, not a handler-wide rule.
- **Tests.** Resolve real cards through the harness (`BaseCardTest`). Every method and `@Nested` class has a `@DisplayName`. No
  white-box wiring tests, and never assert Scryfall metadata. Use `setHand(List.of())` where the random opening hand could
  interfere, and `withAutoStop` rather than adjusting expectations. If unit tests exist for a service you extend, add tests there
  too.
- **What to run.** Run the tests of every handler and service you touched, plus the test classes of every card using a changed
  effect or service path (`python3 -I scripts/effect-inventory.py --tests-for <Class>...` prints the Gradle arguments). Use fully
  qualified names with no wildcards, and one `--tests` list per Gradle test task. Run one Gradle at a time per worktree, always
  with `bash ./gradlew`. Add the whole `magical-vibes-ai` module whenever the AI reads what you changed. Count results from the
  JUnit XML files by name. **Never run the full suite.** Compile every module's main and test sources and all card-test modules
  before reporting.
- **Don't regress** the invariants in `ARCHITECTURE.md` (card freezing, the layer system, thread safety, Jackson 3 imports,
  immutable views).
- **Shared docs.** Don't edit `rules-fixes.md`, `refactors-to-do.md`, `scripts/effect-audit-ledger.csv` or the
  `EffectCountRatchetTest` baselines. Put what they need in the report. `agent-docs/` rows for your own new or changed classes
  are yours to write.
- No banner comments. No `CONVERSION_VERSION` change unless you touch a mapping file AGENTS.md lists; if you do, say so. No
  commits.

## Sequential mode (owner's instruction, 2026-11-27)

The runs are being done one at a time, in wave-table order, each by one fresh agent working **directly on main** (no worktree,
no sync patch, no merge). So the shared-docs rule changes: the agent of a run does its own bookkeeping.

- It fills in its run's "Outcome" line and its track's Status row in this file.
- It deletes from `refactors-to-do.md` entry 18 every line it fixed or dropped. For a line it fixed only partly, it edits the line
  down to what is left. It updates the group's count in the heading.
- It appends each new owner question to entry 17's "Open questions", dated, with the run ID.
- It adds each new defect it saw and did not fix to the later run that owns that area, as the next free `N<n>` line (one
  sentence, "seen in <RUN>"), or to MC-2 if no run fits.
- It adds the ledger rows and moves the `EffectCountRatchetTest` baselines if it deleted or added effect classes.

The orchestrator reviews the diff, runs the architecture tests and a compile of every module, then commits and pushes the run. A
line that needs the owner's answer is left and collected for the end.

## How the orchestrator runs a wave

1. Pick up to **four engine runs** from different tracks whose "Avoid" lists don't name each other, plus **one off-engine run**
   (AI, UI, LG, CL). Follow the wave table below unless a run's outcome changes the picture.
2. Before launching, sync main's uncommitted state into each worktree. Write `git diff HEAD` to a patch, plus a tarball of the
   untracked files. Put the worktrees under the session scratchpad on `/`, because the ramdisk can't hold four.
3. Launch fresh agents: `Agent` with `model: opus`, `effort: medium` and `isolation: worktree` for engine and AI runs. LG-1,
   CL-1 and UI-1 can use `model: sonnet`. Each gets the agent brief with its run ID. Tell each agent to apply the sync patch,
   symlink `card-data-cache`, and never `git stash` (the stash is shared across worktrees).
4. Each agent reports in its final message. Agents can't write report files, so save each report yourself.
5. Merge each worktree into main:
   - Get the diff with `git -C <wt> diff --cached HEAD --binary`, excluding `card-data-cache`, and apply it with
     `git apply -3 --index`.
   - Use a member-level 3-way merge for big files that both sides changed. Resolve the hunks to one side before re-splitting a
     file, and fail loudly on an unterminated member.
   - Then remove the worktree and its branch.
6. Review every diff against the Oracle text, rulings and CR text the report cites. Watch for assertions flipped to match the
   engine, handler-wide rules serving one card, and duplicate edits.
7. Run a combined targeted sweep on main:
   - every test class the wave's reports ran;
   - the architecture tests;
   - the AI module.

   Rerun any failure singly on a HEAD worktree to tell a regression from a known failure.
8. Update the bookkeeping:
   - set this file's Status table and the run's "Outcome" line;
   - delete the fixed and dropped lines from entry 18 (the "Prompt to start it" there points here);
   - add the new owner questions to entry 17;
   - add the ledger rows and ratchet changes.

   Then ask the owner to commit.
9. Before a wave that follows a big shared-service change (DM-1, FC-1, MP-1, EB-1), prune the Gradle build cache entries that
   are more than 2 hours old.

### Wave table

| Wave | Engine runs | Off-engine run |
|---|---|---|
| 1 | DM-1, FC-1, MP-1, CT-1 | AI-1 |
| 2 | DM-2, FC-2, EB-1, PY-1 | AI-2 |
| 3 | DM-3, FC-3, EB-2, CT-2 | UI-1 |
| 4 | TR-1, LS-1, EB-3, PY-2 | CL-1 |
| 5 | TR-2, LS-2, TK-1, MP-2 | LG-1 |
| 6 | TR-3, TK-2, FD-1, PY-3 | |
| 7 | FD-2, LY-1, DR-1, CB-1 | |
| 8 | LY-2, LY-3, PP-1, CP-1 | |
| 9 | LY-4, LY-5, PL-1, MC-1 | |
| 10 | MC-2, FT-1, FT-2 | |
| 11 | Z | |

## Runs

Each run lists its lines, the code it will likely touch (a guess to check, not a fence), what it must not run beside, and notes.
"Outcome" is filled in by the orchestrator.

### 0. Owner questions and bookkeeping (orchestrator, no agent)

**Ask the owner** about the lines the rules don't settle, or that need an infrastructure decision. Some are already waiting in
entry 17. Until the owner answers, each one stays in entry 18 and no run touches it.

- S12 Tribute's "paid" check when an opponent's Halving Season halves the counters.
- T13 Nowhere to Run: should any player's spells and abilities be ignored for hexproof, or only its controller's?
- L2 Should Esix's copies carry the replaced copy's instructions (the T1 rider exclusions)?
- P21 Development: does each accepting opponent let its controller draw a card?
- P25 Madame Null at power 0: the U3b orchestrator decision Q4 vs HEAD's "pay 0 life" pin.
- O11 Set exchanges vs "can't be controlled by other players".
- O21 A seeded game random source (about 180 `ThreadLocalRandom` sites).
- O23 Reality Twist on unlisted land types.
- O24 Boseiju's rider under a land-mana replacement.
- O82 Decoy Gambit when its caster can't draw.
- O93, O97 Teammates' attackers (battle cry, "attacking creatures get"). The engine has no team play, so the question is
  whether to keep these lines at all.
- O6 Cards with no printing identity (conjures, spellbook drafts, hand-built cards): an infrastructure decision.
- O85 The client chooser for an "[A] or [B]" additional cast cost: a missing feature, so should it move to
  `missing-features-to-do.md`?

**Move to entry 16** (effect-cleanup leftovers). These are folds, not rules defects: O18 (two graveyard free-cast offers), O19
(colour-effect merges), O30 (Transcantation's new-targets offer), O79 (Confiscation Coup / Behemoth records; Behemoth's reflexive
"that permanent" stays in entry 18 for MC-2) and O84 (the Demonic Hordes / Infernal Denizen folds; MP-2 takes Infernal Denizen's
opponent choice).

Outcome: 2026-11-27, O18, O19, O30, O79's fold part and O84 moved to entry 16's step-10 parts (O79's Behemoth line stays in
entry 18). The owner's questions are asked once every run is done (owner's instruction).

### DM. Damage

#### DM-1. Pre-pool checks and prevention onto the damage pool

Lines: O9 (about 25 handler pre-checks of `isPreventedFromDealingDamage` skip the damage event), O54 (about 28 handlers still
ask `isDamageSourcePreventedWithLog` / `isDamagePreventedForCreature` first), O57 (the pool's protection rule for fight / bite /
combat), O58 (`resolveAnyTargetDamage` ignores `damageCantBePreventedFromSource`), O62 (player `DamageEvent` has no
can't-be-redirected flag), E13 (Errant Minion and Power Leak deal 2 − X instead of preventing X), F7 (`DamagePreventionService`
per-source preventions and the Aura's printed statics: face-down and lost abilities), O55 (`triggering()` damage logged twice).

Likely touches: `DamageSupport`, `DamagePreventionService`, the damage handlers in `normalfx`, `PlayerDamage*`.
Avoid: LY-3.
Notes: The goal is one way for damage to be prevented, so that every prevention and replacement can be ordered (the D24 / D26
direction; read their Findings in `git show 3b0d60d42d4:effect-cleanup-refactor.md`). Do the handlers in batches by shape, and run
each batch's card tests.

Outcome: 2026-11-27. Fixed: O54, O55, E13, F7 (deleted from entry 18); O9 and O57 fixed for every effect handler and edited
down (O9: Bomb Squad's handler, an owner question in entry 17, and the combat damage step; O57: combat protection). Dropped: O58
(the pool already reads the source's "can't be prevented", pinned in `PrePoolDamageChecksTest`), O62 (no card deals can't-be-redirected
damage to a player; Lava Burst's applies to creatures only). About 35 handler and choice-completion pre-checks gone (CR 616.1);
`isDamageSourcePreventedWithLog` / `isDamagePreventedForCreature` deleted; the pool's protection rule covers any noncombat dealer;
Errant Minion / Power Leak float the paid X as a source-and-player shield; `DamagePreventionService`'s static scans read active
statics. No effect class added or deleted, so no ledger or ratchet change. Tests: 1,072 XML files, 8,968 tests, 0 failures.

#### DM-2. Simultaneous damage and damage results

Depends on: DM-1.
Lines: S1 (multi-dealer damage dealt one dealer at a time: the bite, Rakdos Charm's third mode, Judgment of Alexander), S9
(Disorder deals its damage as two events), S3 and S21 (Ezuri's Predation: the player chooses the pairs, the fights are
simultaneous, and "those creatures" are read when counted), S22 (excess damage ignores other damage dealt at the same time),
E5 (`DealDamageTo…` loses a player hit when a replacement-order prompt reruns it).

Likely touches: `DamageSupport.beginDamageEvent`, `SequenceEffect.simultaneous` / `addSimultaneousEffects`, bite and fight
carriers.
Avoid: TK-2.

Outcome: 2026-11-27. Fixed: S1 (an effect's several dealers, and the sources of a simultaneous event that share a recipient,
deal through `DamageSupport.dealAtOnce`: the recipient is dealt one `DamageDealtEvent.Part.RECIPIENT` event with the total and
the summed excess, each source a `Part.SOURCE` event, so "whenever this is dealt damage" / "whenever you're dealt damage" trigger
once; CR 603.2c, 120.10, the Boros Reckoner ruling; Alpha Brawl, Rakdos Charm and Judgment of Alexander's retaliation), S9
(Disorder's two steps are one simultaneous event; Shadowbane's player half now shares its shield with the creature half for the
open event, either order). Deleted from entry 18. Dropped: S22 (none of the five excess-to-controller cards shares a damage event
with another source, so no other source's damage is dealt at the same time; CR 120.4a), E5 (no reader can see it: the
DEAL_DAMAGE step readers of a player hit, Play with Fire, Mishra's War Machine, Minion of Leshrac and Rite of Consumption, read
one-event steps, and Chandra, Roaring Flame's −7 reads the entry's players-dealt-damage list, which a rerun keeps). Left: S3, S21
(Ezuri's Predation needs a "for each of those creatures, a different one of those tokens" pairing prompt, the same work as entry
16's `CreatedTokensFightDifferentOpponentCreaturesEffect` fold line; the choice is observable only when the tokens differ, e.g.
Chatterfang's Squirrels, ruling "anything else specified ... applies to both"; one step that counts, creates and pairs from one
list also settles S21). No effect class added or deleted, so no ledger or ratchet change. Tests: 528 XML files, 4,992 tests, 0 failures (312 card classes, 23 application classes with the architecture tests, the AI module).

#### DM-3. Damage sources and multipliers

Depends on: DM-2.
Lines: T14 (Eye for an Eye, Harm's Way, Jade Monolith, Shaman en-Kor, General's Regalia and Nova Pentacle build their own
damage-source lists; move them onto `ChooseDamageSourceEffect`), S17 (recipient multipliers vs controller / source multipliers
can't be ordered against each other), O8 (Pestilent Spirit, Forethought Amulet: "the source is a spell"), O20
(`PlayerDamageCandidateSource.context` ignores `explicitSource`), P18 (Gargantuan Gorilla, Bronze Bombshell, Elder Spawn: a
sacrificed source loses its Aura-granted keywords, because the snapshot is read at trigger time).

New line N10 (seen in DM-2): Shadowbane's "you and/or creatures you control" shield is shared between its player and creature
halves only inside a resolution (`playerSourceShieldsUsedInOpenEvent`), so a combat damage step where one chosen trampler damages
a blocker and its controller likely prevents only the first of the two (unverified).

Outcome:

### FC. Free casts and casting from other zones

#### FC-1. Cost increases and the free-cast order

Lines: P6 and P10 (the library, exile, revealed-card, split-card and search free casts pay no cost increase: move them all onto
`payFreeCastCostIncreases` and its mana window), P12 (the free-cast queue asks for the cost-increase mana before modes and
targets), P11 (a mandatory exile free cast offers no kicker), O86 (the free-cast additional-cost chooser is asked after the
targets), H4 (a free cast refused by Teferi / Rule of Law logs "has no legal targets").

Likely touches: `ExileFreeCastQueueSupport`, `LibraryChoiceHandlerService.castCardWithoutPaying`,
`MayCastHandlerService.castRevealedCardWithoutPaying`, `SplitCardCastSupport`, `SpellCastingService`.
Avoid: LS-1, LS-2, CP-1.
Notes: Fold run 18b fixed the graveyard flow; start from it.

Outcome: 2026-11-27. Fixed: P6, P10, P11, H4 (deleted from entry 18); P12 fixed for the "no legal mode or target" case and
edited down (the free casts still ask for the mana before the target is chosen). Left: O86, moved to FC-3 (the chooser must come
before every free-cast flow's target prompt, which is FC-3's one additional-cost path; it changes play for no card). Every
resolution free cast now pays its cost increases (CR 601.2f) through `FreeCastCostIncreaseSupport.payOrAsk`, pool or mana window
(CR 601.2g): `PayFreeCastCostIncreaseEffect` carries a sealed `PausedCast` (exile, queue, hand, library search with its
`IfNotCast` destination, revealed card, library top card, split half) instead of `Resume` + `FreeCastKickerOffer`; the search,
revealed and split castability checks ask `payableWithManaAbilities`. The library-top free plays (`RevealTopCardMayPlayFreeEffect`:
Djinn of Wishes) were fixed too. A mandatory exile free cast offers a mana kicker (CR 601.2b). The library searches log why a
card can't be cast. No effect class added or deleted, so no ledger or ratchet change.

#### FC-2. Faces: Adventures, modal double-faced cards, split cards

Depends on: FC-1.
Lines: P7 (the library `CAST_WITHOUT_PAYING` flow offers no Adventure), T8 (`ImprovisationCapstoneCastChoice` has no face choice),
T9 (`ExileTopCardsAndMayCastSpellsEffect` checks an Adventure against the card's own mana value), P5
(`ExileTopCardMayPlayThisTurnEffect.withAnyManaType()` skips land cards that have an Adventure), P8 (a modal double-faced card
offered by a matching back face can be cast as either face), L6 (split cards' whole-card type and colour prohibitions, not
traced), O1 (casting checks left from runs 40 and 41: Scrycast, Knowledge Pool's "cast only during",
`SplitCardCastSupport.beginFromExile`, the may-cast offers' shared check, suspend's type restriction, and an ability leaving the
stack before it resolves).

Avoid: LS-1, LS-2.

Outcome:

#### FC-3. Additional costs outside the hand

Depends on: FC-2.
Lines: O87 (a graveyard play without paying, through `handlePlayFromGraveyardChoice`, pays no additional cast cost), O88 (a
library-top Bitter Triumph cast with cards in hand is refused), O89 (an exile cast of a "discard a card or pay" spell can't
choose the discard), O90 (a free cast doesn't offer an `AnyOfCost`'s graveyard-exile or forage alternative), O91 (exile and
library-top casts refuse tap, blight and graveyard-exile-or-mana spells), P17 (Flotsam // Jetsam refuses Bone Shards, Minion
Missile and Souls of the Lost), P19 (tap-escalate spells cast through a flashback grant pay no escalate taps).

Notes: One path for picking an additional cost in every cast zone is the aim. If that needs client work, stop at the server part
and report what the client needs (O85 is the owner's question).

Moved from FC-1: O86 (the free-cast "Choose how to pay the additional cost." chooser comes after the targets, CR 601.2b).

New line N4 (seen in FC-1): a graveyard play without paying its mana cost (`MayCastHandlerService.payGraveyardPlayCostIncrease`)
pays its cost increase from the pool only, with no mana-ability window (CR 601.2g); move it onto `FreeCastCostIncreaseSupport`.

New line N5 (seen in FC-1): an exile free cast offers its kicker only when the pool alone pays it
(`ExileFreeCastSupport.offerKickerForFreeCast`), with no mana-ability window (CR 601.2g), and never a kicker with a non-mana cost.

New line N6 (seen in FC-1): Invasion of Alara's chosen card whose cost increase isn't paid goes to the bottom
(`LibraryChoiceHandlerService.handleCastOneAndPutOtherIntoHandChoice`), though the other card already went to the hand, so the
player can't keep the uncast one instead.

Outcome:

### CP. Casting permissions and cost readers

#### CP-1. Permission and cost readers

Lines: F4 (Soulless Jailer reads `card.hasType(CREATURE)`, not the spell), F6 (`CastingPermissionService`'s spell-limit, Ethersworn
Canonist, most-recent-spell and land-restriction readers don't skip face-down sources), P13 (the per-target cost readers: lost
abilities and face down), P3 (Jaya Ballard's emblem grants flashback where the text permits a cast for its mana cost), O22
(Shaman's Trance, Tomik: judged by zone, not by owner), O14 (`hasLukkaExilePermission` matches the name, not the subtype), O15
(Mug's graveyard and exile permissions don't share "a spell").

Likely touches: `CastingPermissionService`, `CastingCostService`.
Avoid: FC-1.

Outcome:

### MP. Multiplayer opponent choices

#### MP-1. "An opponent" chosen in the middle of a resolution

Lines: M15 (seven cards choose "an opponent" after an exile, search or reveal inside a handler: Abstract Performance, Brilliant
Ultimatum, Hostile Negotiations, Ecological Appreciation, Threats Undetected, Turtles Forever, Verdant Mastery), with their
parts of M2, M3, M6, M7 and M8.

Notes: This needs a step that chooses an opponent between two halves of a resolution. First check what exists:
`ChoosePlayerEffect`, `playersThisWay(CHOOSE)` and entry 16's "first half prompts mid-step" cause. If the halves are
`blocked-step-model`, the piece built here should unblock them too: list those rows in the report. Test with three players.

Outcome: 2026-11-27. Fixed: M15 and M8 (deleted from entry 18), and the seven cards' parts of M2, M3, M6 and M7 (edited down;
what is left there is MP-2's). With two or more opponents the controller now chooses the opponent when the "an opponent"
instruction is applied (CR 608.2d; the Abstract Performance and Verdant Mastery rulings), after the exile, search, reveal or
face-up choice, through the new `ChosenOpponentSupport.chooseThen` (`ChosenOpponentFollowUp` records,
`ChosenOpponentContinuation` beans, `PermanentChoiceContext.ChooseOpponentThen`); with one opponent nothing is asked. The
stale first-seat `opponentId` fields of `PendingHostileNegotiations` and `TurtlesForeverSearchChoice` are gone;
`GameTestHarness.addOpponent(name)` seats a third player. None of the seven halves is a `blocked-step-model` row (they are
bespoke handlers and interactions), so no entry 16 row is unblocked. No effect class added or deleted, so no ledger or ratchet
change.

#### MP-2. `getOpponentId` and first-seat readers

Depends on: MP-1.
Lines: the rest of M1 (Elesh Norn's `areOpponentPermanentETBTriggersSuppressed`, Distant Memories, Infernal Denizen), M2 (Shared
Fate, Burning-Rune Demon, Captivating Glance's clash), M3 (Death or Glory, Deliver Unto Evil, Fateseal, Covenant of Minds,
Triskaidekaphobia), M4 (Galactus, Norn's Annex's tax), M5 (Favored Enemy), M6 (Memories Returning, Murmurs from Beyond), M7 (Zur's
Weirding), M9 (`TargetOpponentCreatesTokenEffect`, `OpponentGainsControlOfSourceCreatureEffect`), M12 (Ingenious Mastery), M16
(Wake to Slaughter), M17 (Vislor Turlough with 3+ players), O83 (Varchild's War-Riders, Wall of Shards: cumulative upkeep paid
to the first opponent), H7 (`getRevealedOpponentHand` returns one opponent's hand).

Avoid: PY-1 (Zur's Weirding and the cumulative upkeep payments).
Notes: If this run is too big, split it into MP-2a (M1 to M7) and MP-2b (the rest) and run them in parallel.

Outcome:

### CT. Counters

#### CT-1. Who puts a counter, and on whom

Lines: S13 (experience counters and `MultiPermanentChoiceHandlerService`'s poison counters name no placer: Vorinclex, Halving
Season), S14 (the 6-argument entering `replaceCounters` skips `appliesToAllPermanents` doublers on opponents' permanents), E11
(Suncleanser's player lock isn't asked by the spark cost, proliferate's player-counter writes and five other writers), O65 (The
Serpent Society's poison placer, Perplex's discard cause: settle by the CR, else an owner question).

Likely touches: `PermanentCounterSupport`, `LifeSupport.applyPoisonCounters`, the player-counter writers.

Outcome: 2026-11-27. Fixed: S13, S14, E11 (deleted from entry 18) and O65's poison half (edited down to Perplex, an owner
question in entry 17). Every write of a player counter goes through the new `LifeSupport.givePlayerCounters` (player lock, the
replacements for the named placer, log, "you put counters" event) with `GameQueryService.replacePlayerCounters(.., kind, ..,
placer)`: "you get" is put by the controller (CR 608.2c), proliferate's by the proliferating player (CR 701.34a), a doubling gives
counters (CR 701.10e), Bloatfly Swarm's by its controller, a ransom's by its payer (CR 118.12a), so The Serpent Society's ward
poison is halved by the ward controller's Vorinclex and doubled by the payer's. A positive spark cost can't be paid under
Suncleanser's lock (its ruling) and is replaced like a loyalty cost. The entering `replaceCounters` with a named placer applies
`appliesToAllPermanents` doublers (Primal Vigor on an opponent's unleash counter). Also fixed: proliferate skipped spark counters.
`ExperienceCountersEffectHandler`'s direct put already asked the lock (pinned). `replaceExperienceCounters` deleted. No effect
class added or deleted, so no ledger or ratchet change.

#### CT-2. Counter kinds

Depends on: CT-1.
Lines: P1 (Moxite Refinery's `CounterType.ANY` cost removes kinds in enum order), P20 (Chisei's counter removal takes the first
kind; P1 and P20 need one kind choice per counter, the `SourceCounterCostTypeChoice` generalised), O38 (SILVER treated as a
wildcard), O50 (the spore-counter cards use `FUNGUS`), O53 (`DistributeCountersAmongTargetsEffectHandler`'s delayed removals and
a hexproof gained meanwhile).

Outcome:

### EB. Entry and leave batches

#### EB-1. Through the entry batch

Lines: S4 (Glimpse of Tomorrow, Morphic Tide: each Aura in its own continuation), S7 (Unfinished Business's two attachments one
after the other), T19 (Zara's hand-to-battlefield put calls `handleCreatureEnteredBattlefield` directly), O35 (with two as-enters
choices in one batch, the second is skipped), O2 (outside the entry batch: Painter's Servant's as-enters colour through
`PutCardsOntoBattlefieldEffect`, Karlach, Knickknack Ouphe's Auras), O3 (Lim-Dûl's returned bestow Aura gets no host choice),
P4's Tevesh Szat part (the -10 puts commanders outside the entry batch).

Likely touches: `BattlefieldPlacementService`, the entry batch, `PutCardsOntoBattlefieldEffect`'s handler.
Avoid: TK-1, TK-2.

Outcome:

#### EB-2. "As it would exist on the battlefield"

Depends on: EB-1.
Lines: L10 (`snapshotEnterTappedTypes` / `applyEnterTappedEffects` match printed types), L17 (`EnterPermanentsOfTypesTappedEffect.matching`
judges the permanent as it is), L7 (the returned-permanent animations of Brilliance Unleashed, Tune Up and The War in Heaven apply
after the permanent enters), O4 (Bad Wolf Bay's chaos ability; `CardsCantEnterBattlefieldFromZonesEffect` enforces only graveyard
and library).

Avoid: TK-1.

Outcome:

#### EB-3. Leave batches and departures

Depends on: EB-2.
Lines: O107 (`ExileTargetPermanentEffectHandler`: several plain targets leave one by one, and non-set subjects get no single
event), O106 (Sarulf's exile), S6 (Kairi, Leadership Vacuum, Resounding Wave return one at a time), O78 (Bull Elephant,
Ovinomancer), O27 (Reality Scramble: `PutTargetOnBottomOfLibraryEffect` records no `DepartureResult`), S19
(`PutExiledCardsIntoOwnersGraveyardEffect` gives no graveyard-order prompt), O75 (Acererak's tokens and sacrifices).

Notes: For O78 and O75, first show that the difference is observable (a look-back trigger, a state check). If it isn't, drop
the line with the reason.

Outcome:

### PY. Payments

#### PY-1. Resolution-time payments and "unless"

Lines: P22 and O73 (a one-permanent sacrifice ransom asks the forced pick), P23 and O70 (the ransom logs "an untapped permanent"
and "a Aura"), P24 (`PayXManaCost` is paid with no answering prompt: K'rrik vs Lich's Duel Mastery), P25 (Madame Null at power
0; only if the owner has answered in step 0), O59 (K'rrik's life estimate in `UnlessPaysEffectHandler.canPay`), O64
(`WaterbendPaymentService.canPay` ignores mana not yet made), O76 (`UnlessPaysEffectHandler.nextStep` overwrites the accepted
players), O60 (the "unless" and "may pay" payments with no tax-paid event: Tariff, Winter's Chill, Forgotten Lore and the rest),
O72 (the delayed-trigger special-action payment and Tax Taker; settle from Tax Taker's Oracle text and rulings, else an owner
question), O69 (Ice Cave and Phyrexian / hybrid symbols), O71 (graveyard / exile / command-zone mana abilities during a may-pay
prompt; latent, fix only if cheap).

Likely touches: `ResolutionCostPaymentService`, `UnlessPaysEffectHandler`, `WaterbendPaymentService`.
Avoid: MP-2.

Outcome:

#### PY-2. Mana: spending and producing

Depends on: PY-1.
Lines: P9 and P16 ("as though it were mana of any color" and "mana of any type" applied as the wrong thing, so coloured mana
can't pay {C}), P2 (Feather's {2} per creature is paid only from the pool), P15 (`getActivatedAbilityAdditionalGenericCost` with
a {0} equip or power-up), O25 (Caged Sun misses land mana abilities without {T}), O26 (`TappedLandManaSupport.producedManaTypes`'s
fallback adds mana for a tap that reported none), O37 (an `AwardAnyColorManaEffect` of amount 0 counts every colour), L14 (Kalain
and Coin of Mastery read cast-time mana snapshots by card id), P4's Charmed Pendant and Thran Portal parts.

Outcome:

#### PY-3. Life and discard costs

Depends on: PY-2.
Lines: O61 (the morph face-up life cost and the attack / block life taxes check `canPlayerLifeChange`, not `canPlayerLoseLife`),
O63 (Library of Leng's replacement is offered for a discard paid as a cost), P4's Animation Module part (offers only poison and
energy player counters).

Outcome:

### TR. Triggers, subjects and targeting

#### TR-1. Triggers

Lines: S10 (an additional trigger instance, made by `TriggerAwareStackList.add`'s copy, keeps the first instance's targets and
modes), F9 (`areCreatureDeathTriggersSuppressed`'s simultaneous-death scan), O51 (Voracious Brood: one trigger per card, not per
event), O56 ("this creature or another [X] enters" records no triggering permanent when the source triggers itself), O95
(`triggerOrderLabel` names a source-reading boost "target creature"), O39's trigger half (Coastal Piracy's to-player trigger has
no `AttackedTargetIsOpponent` condition; CB-1 takes it if CB-1 runs first).

Likely touches: `TriggerCollectionService`, `TriggeredAbilityQueueService`, `GameData.TriggerAwareStackList`.

New line N8 (seen in CT-1): `EnergyCountersEffectHandler` ("you get {E}") raises no "you put counters" event, so All Will Be
One doesn't trigger on its controller getting energy (its ruling: any time you put counters on a player).

Outcome:

#### TR-2. Subjects and last-known information

Depends on: TR-1.
Lines: T2 (`CardSubject.TriggeringCards` reads no type for a card that left the zone its event put it in), T10
(`SubjectResolutionService.attachedId` with no source snapshot), O10 (`TriggeringPermanentPower` / `Toughness` prefer the
trigger-time value), O40 (Terminal Velocity's then-steps over `Subjects.source()`), E12 (`handleLookAtTopCardPutLandOrCreatureChoice`'s
Aura put records no RETURN step), S11 (`ParleyEffect`'s token form creates tokens inside its handler).

Likely touches: `SubjectResolutionService`, `DynamicAmount` readers, `MayMiscHandlerService`.
Avoid: FD-2.

Outcome:

#### TR-3. Target and choice prompts

Depends on: TR-2.
Lines: T5 (`beginAnyTargetChoice` and the permanent-or-player prompt don't drop what can't be chosen), T6 (a name prompt that
requires a type uses a copy's types), T20 (Descendants' Fury offers creatures that can't be sacrificed), T17
(`ExileGraveyardCardsEffect.defendingPlayerOnly` is never read), T22 (Rasaad's specialize target spec), T3 (Soundwave's "from their
graveyard"), O29 (`CounterSpellEffect` with several targets and `LIBRARY_TOP_OR_BOTTOM` opens overlapping prompts), O31 (Bow of
Nylea and The Moonbase: the activation path needs the `targetGraveyardCards` branch the cast path got in fold run 50c).

Outcome:

### LS. Library searches and reveals

#### LS-1. Whether to search, and the shuffle

Lines: E2 (may-searches have no "do you search?"), E3 (a same-name search with no match asks nothing; Haunting Echoes with nothing
exiled), E9 (a prevented search still shuffles), E10 (Last Light of Durin's Day always searches the library), E15 (Journey for the
Elixir doesn't shuffle after taking both cards from the graveyard).

Likely touches: `LibraryChoiceHandlerService`, `SearchLibraryAndOrGraveyardForCardToHandEffect`'s handler.
Avoid: FC-1, FC-2.

Outcome:

#### LS-2. What a search and a draft record

Depends on: LS-1.
Lines: E7 (the library-search flows handled before the generic path record no found cards in SEARCH), E8 and H2 (the
`LibraryRevealChoice` drafts record no CONJURE step; Support Skyforge exiles the cards it didn't choose face up and names them),
O32 (Panglacial Wurm can be cast only on the first pick of a multi-pick search), T4 (Opposition Agent's player can take a cast-only
card; the AI's search strategy answers only for the searcher).

Avoid: FC-1, FC-2.

Outcome:

### TK. Tokens and copies

#### TK-1. Token replacements

Lines: S15 (`TokenKindReplacements` and `JinnieFayTokenReplacements` read `event.controllerId()`'s battlefield), S16 and L5 (the
two fixed-order `getTokenMultiplier` callers: `CreateTokensAttachedToControlledCreaturesEffectHandler` and
`ReturnTargetCreatureToHandAndCreateTokenCopyEffectHandler`), L3 (Life of the Party and Wedding Ring offer no first-token-creation
replacement), L4 (a token copy of a source reads `cardToCopy`, not `copiableCard`), O68 (Timothar: only the last Bat is linked).

Avoid: EB-1, EB-2.
Notes: L2 (Esix's instructions) is the owner's. Don't change the rider exclusions T1 kept.

Outcome:

#### TK-2. Copies of spells and permanents

Depends on: TK-1.
Lines: S18 (Precursor Golem, Ink-Treader Nephilim: the copies' controller doesn't choose the stack order), T11 (Kefnet, Reversal
of Fortune, Spellweaver Volute: the copy is cast in exile, not in the card's zone), T12 (The Clone Saga chapter II offers new
targets), O99 (`CloneService.exileCopiedCardAbility` exiles a card that left the graveyard and came back), T1
(`ChoosePermanentsEffect` keeps a lone chosen permanent as the source's chosen permanent).

Avoid: DM-2.

Outcome:

### FD. Face-down and hidden reads

#### FD-1. Colours and static-set reads

Lines: F8 (`getEffectiveCardColors`' off-battlefield scan asks only the floating flag), F12 (Painter's Servant doesn't colour
cards off the battlefield or spells), F11 (`layeredQueryReads`' `matchesLeaf` uses printed keywords and types).

Likely touches: `GameQueryService`.
Avoid: PP-1.

New line N1 (seen in DM-1): `GameQueryService.hasAuraWithEffect` reads an attachment's printed statics with no lost-ability
check, so the other Aura and Equipment prevention steps share F7's defect.

Outcome:

#### FD-2. Hidden faces

Lines: F2 (`cardsThisWay`, the copy, cast and name / mana-value readers and the entry batch's "can't enter" filter read a face-down
exiled card's hidden face), F3 (token copies of face-down permanents and other readers of real characteristics; not looked at, so
sweep first and report), T15 (the copy-service turn-face-up path doesn't check "can't be turned face up"), H1 (`cardsThisWay(REVEAL)`
names a card that a later step reordered), H9 (`GameViewProjectionFactory.collectFaceDownReveals` vs `mayLookAtExiledCard`;
`exilesHandsFaceDown` doesn't look inside `MayEffect`).

Avoid: TR-2.

Outcome:

### LY. Layers, protection, text changes, linked abilities

#### LY-1. Rules of the game vs granted abilities

Lines: F1 (Kappa Cannoneer, Lockjaw, Slicer, Full Steam Ahead, Vines of Vastwood: "can't be blocked / sacrificed / the target"
modelled as granted abilities that "loses all abilities" removes), F5 (Stoic Star-Captain, Zilortha), O105 (Rikku's "can't be
blocked by" floated as a granted ability).

Notes: One way to float a rule that is not an ability is the aim. Read `agent-docs/LAYER_SYSTEM.md`.

Outcome:

#### LY-2. Type-changing effects as floating effects

Lines: L15 (`BecomeCreatureEffect`, used by 24 cards, replaces the card instead of floating a type-changing effect and a P/T-setting
effect), L1 (Wickerwing Effigy; full line in the queue file), L9 (Starfield of Nyx: `isAnimatedByStarfield` ignores later type
removals, and `wouldEnterAsCreature` ignores Starfield).

Avoid: LY-1, LY-5.

Outcome:

#### LY-3. Protection

Lines: L12 (`ProtectionFromColorsEffectHandler` gives a self-subject protection to the host when its source is attached), L13 (Mask
of Law and Grace, Shield of Duty and Reason, Strength of Isolation and Strength of Lunacy stay exempt from their own protection
when recoloured).

Avoid: DM-1.

Outcome:

#### LY-4. Text changes

Lines: L18 (`TextChangeTransformer` rewrites change effect identity, so a targeted trigger can lose its target group), L19 (a text
change on a spell on the stack, Sleight of Mind's `spellTextReplacements`), O94 (text changes reach only some boost amounts and
filters).

Outcome:

#### LY-5. Borrowed and linked abilities

Lines: L11 (ability borrowers in a dependency loop are approximated by assembly order), O33 (Valki and other "becomes a copy" cards:
the copied return reads cards the copier exiled with its own abilities), O48 (Myr Welder, Territory Forge, Idris, Locus of
Enlightenment: printed exiles record no link), O49 (Prototype Portal's borrowed "X is the mana value of the exiled card").

Outcome:

### DR. Draws

#### DR-1. `DrawService`

Lines: M1's draw part (Notion Thief, Hullbreacher, Alms Collector look at one opponent), M13 (two such sources under different
opponents apply in seat order; the affected player should choose), F10 (Alms Collector, Words-return and Quantum Riddler readers
skip no face-down source), E4 (a draw replaced by exactly one draw still reveals), E6 (`DrawCardEffectHandler` records no cards when
a draw is replaced by drawing more), O101 (`EachOtherPlayerDrawsCardEffectHandler` draws in seat order, not APNAP).

Likely touches: `DrawService`, `DrawCardEffectHandler`, `EachOtherPlayerDrawsCardEffectHandler`.
Notes: O101 is the gift cards. The gift opponent is the owner's deferred question (entry 17), so fix only the draw order.

Outcome:

### CB. Combat

#### CB-1. Defending players and attack requirements

Lines: M11 (`handleDeclareBlockersStep`, `getBlockableAttackerIndices` and `defendingPlayerControlsMatchingPermanent` take the
defending player as `getOpponentId`), M10 (`MayEffect`'s `DEFENDING_PLAYER` doesn't read the recorded defending player; Soldevi
Heretic), M5 (Stand or Fall: "each defending player", and a floated `OnlyChosenCreaturesCanBlockEffect` on every player), M14
(`ReturnCardExiledWithSourceToBattlefieldEffectHandler`: an attacking creature with no attack target), M4's Nahiri part
(`MustAttackPlayerEffect`'s count), E14 (`CombatAttackService` throws for `MustAttackIfAbleEffect(defenderId)` instead of weighing
it against other requirements), O39 (Coastal Piracy and "deals combat damage to an opponent"), O36 (goading a creature the same
player already goaded adds another goad), H8 (`CantAttackThisTurnEffectHandler`'s set branch logs a face-down source's name and
binds no chosen values), S23 (`isCantAttackDueToGlobalRestriction`'s planechase branch; latent, fix only if cheap).

Likely touches: `CombatBlockService`, `CombatAttackService`, `AttackLegalityService`, `ConditionEvaluationService`.
Notes: Test with three players.

Outcome:

### PP. Perpetual changes

#### PP-1. Perpetual changes follow the card

Lines: L16 (five perpetual handlers write into an exchanged card, which a copy then copies), O41 (`PerpetuallyGrantKeywordToSourceEffect`
puts the keyword onto a copy of the permanent's card and finds a card by id in any zone), O42
(`PerpetuallyGrantStaticEffectToSourceEffect`), O43 (`PerpetuallyBoostSourceEffect`'s graveyard path rewrites printed P/T), O45
(`RemovePerpetualTriggeredAbilityEffect`, Monoist Gravliner: found by id in any zone), O44 (Rothga reads a face-down spell's real X),
O47 (a perpetual evoke beside another perpetual alternative cost: `findGrantedAlternateCast` offers the first only), L8 (Thought
Partition and Hydroponics Architect write colour, mana cost and types into runtime copies).

Notes: Run PO's perpetual P/T store on graveyard cards (`PerpetuallyBoostCardEffect`) is the model: one perpetual store, read by
the layered pass, keyed so that a new object doesn't inherit it. Read its Findings section.
Avoid: FD-1.

Outcome:

### PL. Each player chooses, then all act

#### PL-1. Choices first, then one action

Lines: S2 (Disorienting Choice, Eumidian Wastewaker act at each player's answer), S5 (Ol Rin's Searing Light, Gilded Ambusher,
Dispersal, The Second Doctor, Kwain), S20 and O34 (Eumidian Wastewaker counts a land token, a land only on the battlefield, or a
land a replacement exiled).

Notes: First check what entry 16's "Each player chooses, then all act (blocked-player-loop)" cause still lacks. The piece built
here should serve those rows too: list them in the report.

Outcome:

### MC. Single cards

#### MC-1

Lines: T7 and T18 (the opponent-owned processor head and Ulamog's Nullifier: piles, and random face-down picks), E16
(`CoinFlipService`'s Pokey d20 ignores dice advantage), O12 (Enduring Angel that can't transform), O13 (Fear of Sleep Paralysis'
lock vs other stun removals), O66 (Gix; full line in the queue file), O81 (Sword-Point Diplomacy's processing order, which its ruling
lets the caster choose).

Outcome:

#### MC-2

Lines: O5 (Limitless Rekindling's pool), O7 (`ConjureCardToBattlefieldEffectHandler`: a conjured land gets no land-enter handling;
check that the handler still exists after run J), S8 (Mythweaver Poq's simultaneous duplicates: rulings first, else an owner
question), the Behemoth of Vault 0 part of O79 (a reflexive ability about an earlier target).

S3 and S21 (Ezuri's Predation, left by DM-2): build the "for each of those creatures, choose a different token" pairing prompt
(the same piece entry 16's `CreatedTokensFightDifferentOpponentCreaturesEffect` line needs), one step that counts, creates and
pairs from one list, and simultaneous fights.

Outcome:

### AI. Hard / easy AI (`magical-vibes-ai`)

#### AI-1. "Unless" and payment scoring

Lines: O67 (a ward or "counter unless" asked of the AI about its own spell scores negative), O74 (branches aimed at the payer
other than life loss are valued from the AI's side), O77 (an "unless" branch that sacrifices its own source scores as nothing;
`UpkeepRansomAiTest` pins today's parity), O80 (`UnlessPaysEffect.forEach` scores the branch as a whole), O28
(`PayFreeCastCostIncreaseEffect`'s "may" likely scores 0), P14 (`XValueChoiceAiStrategy` pays its whole life).

Outcome: 2026-11-27. Fixed: O67, O74, O77, O80, O28, P14 (deleted from entry 18). The hard AI scores an "unless" it is asked
through the new `SpellEvaluator.unlessPaymentValue`: a branch is read as its payer sees it (CR 118.12a); one written by
another player's ability is scored from that controller's side and its sign turned, so ward or Mana Leak on its own spell
(CR 702.21a) and a discard aimed at it (Tainted Specter) are its losses; a branch that sacrifices,
destroys, exiles, bounces or phases out the source is the source's value (Phantasmal Forces is kept, `UpkeepRansomAiTest`
flipped); "for each [object] … unless" is the object asked about (Cleansing: its own land kept, an opponent's not). A free
cast's cost increase is scored as the spell it casts. `XValueChoice` gained `lifePayment` (`XValueChoice.lifePayment`, used by
`EachPlayerPaysAnyManaEffect`'s LIFE and `EachPlayerPaysAnyLifeForTokensEffect`); the AI pays at most half its life (CR 119.4).
No effect class added or deleted, so no ledger or ratchet change.

#### AI-2. Targeting and value

Depends on: AI-1.
Lines: O92 (`BoostEffect.targetPermanent` declares a harmful spec whatever the sign; this is a domain change, so grep its readers),
O103 (`TargetPolarityClassifier` and the `ChooseOneFor…` records), O52 (Clash of the Eikons, Sigurd, Etched Host Doombringer,
Portent Tracker: a fixed-kind removal scored by kind), O108 (destroy all a target player controls, scored over both battlefields),
O98 (set damage has no value), O102 (`drawScore` for "each opponent draws"), T16 (the filtered must-be-blocked choice with
menace), T21 (Avatar's Wrath's airbend scored as single-target exile).

Notes: Keep any owner decision recorded in a line (O52: 2026-11-25). A change to a decision rule needs a pin test for the old
case it must keep.

New line N9 (seen in AI-1): an "unless" branch `SpellEvaluator` has no score for (Wand of Ith's `DiscardCardsEffect` of the
revealed card) is worth 0 to the hard AI either way, so it never pays to keep the card; unverified by a test.

Outcome:

### UI. Frontend

#### UI-1. Multi-permanent pick minimum

Lines: O17 (the client ignores `MULTI_PERMANENT_PICK`'s minimum and shows graveyards for an index pick over a pool;
`GameSimulator` answers a multi-pick with the first maximum).

Outcome:

### LG, CL. Logs and non-rules cleanup

#### LG-1. Log lines

Lines: H3 (`ChooseCardEffectHandler` logs a revealed card in hand unnamed; keep hidden-ness on the subject, since a zone check broke
Addle and Intuition before), H5 (`SetBasePowerToughnessEffectHandler` prints "null"), H6 (Shadow of Doubt logs nothing), H10
(`ClashSupport` doesn't name the player).

Avoid: CB-1 (H8 is there).

New line N2 (seen in DM-1): the Healing Grace shield's order-prompt label says "(Healing Grace)" for Errant Minion's shield too.

Outcome:

#### CL-1. Dead code and a misnamed test

Lines: O46 (`RevealedHandChoice.bottomThenDrawMode` / `discardThenDrawMode` are dead), O100 (`ActivatedAbilityExecutionService`'s
inline mana-ability draw branch is likely dead), O96 (`BoostEffectHandler`'s snapshot refresh changes nothing), O104
(`TheGrimCaptainsLockerTest.doesNotGrantEscapeToNonCreatureCards` doesn't test what its name says).

New line N3 (seen in DM-1): unused fields left in `DamageSupport`, `MultiPermanentChoiceHandlerService` and
`PermanentChoiceBattlefieldHandlerService`.

New line N7 (seen in MP-1): about 100 card test classes carry their own copy of a private `addOpponent` / `addThirdPlayer`
helper; they can use `GameTestHarness.addOpponent(name)` instead (Abstract Performance's, Hostile Negotiations' and Threats
Undetected's older test methods also have no `@DisplayName`).

Outcome:

### FT. Missing features

#### FT-1. The Ring's levels 2 to 4

Lines: E1. The Ring tempts you: implement the remaining abilities, verified against the CR's Ring rule (find it with
`search_rules`) and the rulings of the tempting cards.

Outcome:

#### FT-2. A plot destination

Lines: O16 (Make Your Own Luck puts all three cards into hand, then plots the chosen one).

Outcome:

### Z. Close (orchestrator)

- Check that every ID has an outcome, or is waiting on the owner with its entry 17 question.
- Ask the owner for a full suite run.
- Rewrite entry 18 to hold only what is left (owner-waiting and newly found lines, with their IDs), and point its prompt at this
  file's history.
- Update `agent-docs/` for every new shared piece (effect-building-blocks.md first).
- Delete this file when nothing in it is open.

Outcome:

## Agent brief

Replace `<RUN>` with the run ID.

```
Do run <RUN> of rules-fixes.md. Read its "Ground rules for every run" and your run's section; print your lines with the
awk command under "Line IDs" and read each one's full queue line (git show 3b0d60d42d4:effect-cleanup-queues.md, grep the card
or class). Read AGENTS.md and agent-docs/ARCHITECTURE.md, then the agent-docs/architecture/ topic file for your area.
You work in a git worktree: first apply the sync patch and untracked-file tarball named here: <PATCH>, <TARBALL>, then
symlink card-data-cache from /mnt/ramdisk/magical-vibes. Never git stash, never touch the main checkout.
For each line: confirm it with a test that fails on the current code, then fix it; check Oracle text and rulings with the
Scryfall MCP and every CR number (the line's included) with get_rule. A line that isn't a defect is dropped with the reason;
one the rules and rulings don't settle is left unchanged as an owner question. Run only the tests the ground rules name, with
fully qualified class names, one Gradle at a time; never the full suite. Don't edit rules-fixes.md, refactors-to-do.md, the
ledger or the ratchet baselines. Don't commit.
End with a report in your final message: per line ID, fixed (the test that failed first, the Oracle text / ruling / CR rule),
dropped (why), or left (why, or the owner question); new or changed shared pieces and their agent-docs rows; files touched;
tests run, with counts from the XML; anything new you saw and did not fix.
```

## Orchestrator prompt

```
Run wave <N> of rules-fixes.md (or: run <RUN>). Read the file's "How the orchestrator runs a wave", the wave table and the runs
in the wave; check each run's "Depends on" is done. Launch one fresh agent per run with the agent brief (opus, medium effort,
worktree isolation; sonnet for LG/CL/UI), merge their worktrees into main, review the diffs, run the combined targeted sweep,
then update this file's Status and Outcome lines, entry 18 and entry 17, and tell me what changed and what needs my decision.
Don't commit until I say so.
```
