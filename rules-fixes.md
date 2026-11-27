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
| DM Damage | DM-1, DM-2, DM-3 | DM-1, DM-2, DM-3 done 2026-11-27 |
| FC Free casts and casting from other zones | FC-1, FC-2, FC-3, FC-4 | FC-1, FC-2, FC-3 done 2026-11-27 |
| CP Casting permissions and cost readers | CP-1 | open |
| MP Multiplayer opponent choices | MP-1, MP-2 | MP-1, MP-2 done 2026-11-27 (Triskaidekaphobia, H7 for the owner) |
| CT Counters | CT-1, CT-2 | CT-1, CT-2 done 2026-11-27 |
| EB Entry and leave batches | EB-1, EB-2, EB-3 | EB-1, EB-2, EB-3 done 2026-11-27 |
| PY Payments | PY-1, PY-2, PY-3 | PY-1, PY-2 done 2026-11-27; PY-3 open |
| TR Triggers, subjects and targeting | TR-1, TR-2, TR-3 | TR-1, TR-2, TR-3 done 2026-11-27 (TR-3: T5's caller sweep left) |
| LS Library searches and reveals | LS-1, LS-2, LS-3 | LS-1 done 2026-11-27; LS-2 done 2026-11-27 (N35, E10 left) |
| TK Tokens and copies | TK-1, TK-2 | TK-1 done 2026-11-27; TK-2 open |
| FD Face-down and hidden reads | FD-1, FD-2 | open |
| LY Layers, protection, text changes, linked abilities | LY-1 to LY-5 | open |
| DR Draws | DR-1 | open |
| CB Combat | CB-1 | open |
| PP Perpetual changes | PP-1 | open |
| PL Each player chooses, then all act | PL-1 | open |
| MC Single cards | MC-1, MC-2 | open |
| AI Hard / easy AI | AI-1, AI-2 | AI-1, AI-2 done 2026-11-27 |
| UI Frontend | UI-1 | UI-1 done 2026-11-27 |
| LG, CL Logs and non-rules cleanup | LG-1, CL-1 | CL-1, LG-1 done 2026-11-27 |
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
| 10 | MC-2, FT-1, FT-2, FC-4, LS-3 | |
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

Outcome: 2026-11-27. Fixed: S17 (every source, controller and recipient multiplier is its own pool option through
`GameQueryService.DamageMultiplier` lists and `DamageCandidateSource.multiplierRule`, so a "plus N" can go between Gisela's and
Goldnight Castigator's doublings or two Fiery Emancipations; CR 616.1; the int multiplier queries are now the lists' product),
T14 (the seven chosen-source handlers ask from `DamageSourceChoiceSupport.choosable`, CR 609.7a's full set: graveyard cards a
stack object targets, waiting shields' sources; Eye for an Eye's completion takes them too and no longer fails on an ability
source), O8's Forethought Amulet half (an ability of an instant or sorcery card is an instant or sorcery source: Resounding
Thunder's cycling trigger), O20 (a fought creature's damage redirected to a player is dealt through that creature's own entry,
`DamageEvent.dealtThrough`, so its lifelink applies; CR 120.1, 702.15b), N10 (Shadowbane's shared shield also covers a combat
damage step, CR 510.2). Deleted from entry 18. Dropped: P18 (a source sacrificed earlier in its own resolution already deals
damage with its layered last-known keywords, an Aura's lifelink included: `PermanentRemovalService` rewrites the resolving entry's
source snapshot; pinned in `GargantuanGorillaTest`, `ElderSpawnTest`), O8's Pestilent Spirit half (fixed by D19; pinned in
`PestilentSpiritTest`). The fold half of T14 (the six cards onto `ChooseDamageSourceEffect` plus a step reading the choice) is a
cleanup left for entry 16. No effect class added or deleted, so no ledger or ratchet change. Tests: 565 XML files, 5,382 tests,
0 failures (295 card classes, 148 application classes with the architecture tests, the AI module).

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

Outcome: 2026-11-27. Fixed: P7, P8, T8, T9, P5, L6 (deleted from entry 18); O1 fixed in part and edited down (Knowledge Pool now
asks the chosen card's own "cast only during" and cast condition, per its ruling; `SplitCardCastSupport.beginFromExile` no longer
judges the whole split card, each half is judged as offered, CR 709.3a, which was also L6's one real case (Gaddock Teeg and
Fire // Ice); suspending asks every casting prohibition, CR 702.62a, 116.2f, 601.3). Left in O1: Scrycast (Biting Remark has no
Oracle data in Scryfall or MTGJSON, and the fix needs the scry answer and both AI strategies), the may-cast offers' shared check
and CR 608.2n's ability removal (not reached). An effect's cast now judges only the face cast (CR 716.3, 716.3a, 712.8b): the
library `CAST_WITHOUT_PAYING` flow carries each card's matching faces (`LibrarySearchParams.offeredFaces`, new `SpellFace`),
casts an Adventure (`LibraryCastSpellTarget.physicalCard`, `LibraryCast.physicalCard`/`faces`) and asks which face when both
match (`ChoiceContext.LibraryFreeCastFaceChoice`); the free-cast queue's producers offer through
`ExileFreeCastQueueSupport.offerFaces` (Primeval Spawn and the others on `ExileTopCardsAndMayCastSpellsEffect`, every
`CastCardsWithoutPayingManaCostEffect` card, which now also offers a land card's Adventure), the queue asks the face as it
reaches a card both of whose faces fit (`GameData.pendingFreeCastEitherFaceIds`, `ChoiceContext.FreeCastQueueFaceChoice`) and the
total cap counts the cheaper face. Mana of any type pays a land card's Adventure (`withAnyManaType`, the FIN towns' rulings). No
effect class added or deleted, so no ledger or ratchet change. New lines N11 (CP-1), N12 and N13 (FC-3).

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

New line N12 (seen in FC-2): the other free-cast queue producers don't offer through `ExileFreeCastQueueSupport.offerFaces`, so
they refuse a land card's Adventure or give no face choice: `ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffectHandler`,
`ExileRandomCardFromEachOpponentGraveyardMayCastFreeEffectHandler`, `EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffectHandler`
and `PlarggAndNassariCardChoiceInteractionHandler` (CR 716.3).

New line N13 (seen in FC-2): `ExileTopCardsAndMayCastSpellsEffectHandler` judges a modal double-faced card by its front face only,
and the total cap counts the front's mana value, though Primeval Spawn's ruling lets its back face be cast and counted (CR 712.8b).

Outcome: 2026-11-27. Fixed: P19, P17, O87, O88, O89 (deleted from entry 18); O91 edited down (library top now takes an
"[A] or [B]" cost of sacrifice, mana, life and discard alternatives; tap, blight and graveyard-exile alternatives are still
refused from exile and library top). A flashback-granted graveyard cast validates and taps a tap escalate's permanents for each
mode beyond the first (CR 702.120a). A graveyard play without paying its mana cost (`handlePlayFromGraveyardChoice`: Horde of
Notions, Kaya, Jetsam) asks `ExileFreeCastQueueSupport.preparedCastCostsPayable` and pays through `beginSacrificeCostIfNeeded`
after the targets, permanent spells too (CR 601.2f, 601.2h; Jetsam's ruling); P17's CR 118.9d was the wrong citation. The
library-top and exile casts take `discardHandCardIndex` (`PlayCardRequest` already carried it; new `GameService` overloads,
the dispatch, `LibraryTopCastReplay`, `ExileCastReplay`), and the library top picks its alternative with
`chosenAlternative` (CR 601.2b, 119.4). Left, not reached: O86, O90, O91's rest, N4, N5, N12, N13, and N6 (only the declined
cost-increase case remains: the ruling keeps the uncast card in exile and lets the caster choose the hand card, which needs a
continuation after the cast attempt). No effect class added or deleted, so no ledger or ratchet change. New line N26 (MC-2).

#### FC-4. What FC-3 didn't reach

Depends on: FC-3.
Lines: O86 (the free-cast additional-cost chooser is asked after the targets), O90 (a free cast doesn't offer an `AnyOfCost`'s
graveyard-exile or forage alternative), O91's rest (exile and library-top casts refuse tap, blight and graveyard-exile options),
N4, N5, N6 (the declined-payment half: the uncast card stays in exile and the caster chooses the hand card), N12, N13. Read
FC-3's Outcome first. If a line needs client work, do the server part and add the client part to UI-1.

New line N32 (seen in CT-2): `SpellCastingService.removeOneCounter` pays a `CounterType.ANY` casting cost (Falco Spara,
Pactweaver's "removing a counter from a creature you control") by removing a -1/-1 counter, else a +1/+1 counter, so from a
creature with only other kinds (Falco's own shield counter) it removes nothing, and with several kinds the caster never chooses;
sure of the code, not tested.

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

New line N11 (seen in FC-2): `ExileTopCardMayPlayThisTurnEffect`'s "you may cast that card" (`castOnly`: Ragavan, Daxos) gives
a land card with an Adventure (the FIN towns) no permission, though the towns' rulings let such an effect cast the Adventure.

New line N37 (seen in PY-2): the castability readers of the any-mana permissions (`GameViewProjectionFactory`'s exile
`getAnyManaTypeExiledCardIds` and library-top checks, `GameActionAvailabilityService.canSpendAnyManaTypeToCast` with Xanathar's
library-top grant) check `canPayAsGeneric`, so an "as though it were mana of any color" permission (`AnyManaSpending`) shows a {C}
cost as castable with colored mana that the payment then refuses (CR 107.4c); read `CastingPermissionService.anyManaSpending`.

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

Outcome: 2026-11-27. Fixed, each with a three-player test that failed first: Infernal Denizen and Rogue Skycaptain (the
controller chooses the opponent as the ability resolves, `opponentToBeChosenFirst` and an engine-set `PlayerSubject` on
`OpponentMayGainControlOfCreatureYouControlEffect` / `OpponentGainsControlOfSourceCreatureEffect`; Rogue Skycaptain ruling,
CR 608.2d), Shared Fate (the drawer chooses among opponents with a card, each replaced draw: `ChosenOpponentSupport.askThen`,
`ChosenOpponentFollowUp.SharedFateExile`, `SharedFateLibraryContinuation`), Galactus (every opponent tied for the most life
satisfies it: `CombatAttackRequirementEffect.requiredAttackTargetIds` is now a set, read by `AttackLegalityService`), Norn's
Annex (each attacker pays the attacked player's Phyrexian tax: `CastingCostService.getPhyrexianAttackPaymentsPerCreature(..,
attackTargetId)`), Favored Enemy (`ChooseOpponentOnEnterEffect` + the note waiting for it; the noncreature spell path now asks
the as-enters opponent choice, `AsEntersInteractionService.beginOpponentChoiceAsEnters`; Jihad, Energy Vortex and Call to Arms take
that path too, untested with three players), Zur's Weirding (each other player who can pay is offered it in APNAP order, CR 101.4),
Ingenious Mastery (`ChoosePlayerEffect.opponent()` after the draw, `TargetOpponentCreatesTokenEffect.createdBy`, scry by
`playersThisWay(CHOOSE)`; its multiplayer ruling), Wake to Slaughter (`opponentToBeChosenFirst`), Varchild's War-Riders and
Wall of Shards (the opponent chosen per age counter: `OpponentGainsLifeCost(amount, payments)`, the player choice on
`PermanentChoiceContext.ResolutionCostPayment`; their rulings). Dropped: M17 Vislor Turlough (the three-player test passes on
HEAD; kept as a pin), and the parts of M1 to M6 already fixed before this run (Elesh Norn, Distant Memories, Burning-Rune
Demon, Captivating Glance, Death or Glory, Deliver Unto Evil, Fateseal, Covenant of Minds, Memories Returning, Murmurs from
Beyond: read in the code, three-player tests exist or were added). Left: Triskaidekaphobia (no player elimination) and H7
(one revealed opponent hand in the view): owner question in entry 17; M9's gift branch (the gift-opponent question); M1's
draw part (DR-1), M4's Nahiri part and M5's Stand or Fall (CB-1). New lines N48 (CB-1), N49 and N50 (MC-2). No effect class
added or deleted, so no ledger or ratchet change.

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

Outcome: 2026-11-27. Fixed: P1, O50. Dropped: P20, O38, O53. All deleted from entry 18. P1: Moxite Refinery's "remove X
counters" from a permanent with several kinds and more than X counters now asks the kind of each counter, one at a time
(CR 601.2h via 602.2b: the payer pays the cost), through `ChoiceContext.ControlledCounterCostTypeChoice`, which gained
`required` and `chosenSoFar` like `SourceCounterCostTypeChoice`; the forced picks are filled by one helper both choices share,
and `RemoveCounterFromControlledPermanentsCostHandler` spends `GameData.chosenCostCounterTypes`. O50: the 19 spore-counter cards
(Oracle "spore counter") now use `CounterType.SPORE`; only Sporogenesis and Mindbender Spores keep `FUNGUS` (CR 122.1: only
counters with the same name are interchangeable), so Thelon of Havenwood no longer counts fungus counters and Sporoloth Ancient's
granted ability spends Thallid Shell-Dweller's spore counters. P20 not a defect: since PY-1 the resolution-time removal asks the
payer the permanent and kind (Chisei's "ki counters" test already pins it). O38 unreachable: silver counters exist only on cards
exiled by Karn, Scion of Urza, and counters cease to exist when the card leaves exile (CR 122.2), so no permanent can have one;
`Permanent.setCounterCount` rejects SILVER. O53 not a defect: the delayed removal declares no target, so a shroud gained
meanwhile doesn't stop it (CR 603.7c; pinned in `SystemsOverrideTest`). New line N32 (FC-4). No effect class added or deleted,
so no ledger or ratchet change.

### EB. Entry and leave batches

#### EB-1. Through the entry batch

Lines: S4 (Glimpse of Tomorrow, Morphic Tide: each Aura in its own continuation), S7 (Unfinished Business's two attachments one
after the other), T19 (Zara's hand-to-battlefield put calls `handleCreatureEnteredBattlefield` directly), O35 (with two as-enters
choices in one batch, the second is skipped), O2 (outside the entry batch: Painter's Servant's as-enters colour through
`PutCardsOntoBattlefieldEffect`, Karlach, Knickknack Ouphe's Auras), O3 (Lim-Dûl's returned bestow Aura gets no host choice),
P4's Tevesh Szat part (the -10 puts commanders outside the entry batch).

Likely touches: `BattlefieldPlacementService`, the entry batch, `PutCardsOntoBattlefieldEffect`'s handler.
Avoid: TK-1, TK-2.

Outcome: 2026-11-27. Fixed: S4, S7, O35 (deleted from entry 18); O3 fixed for Lim-Dûl's head and edited down (the other
dying-creature return heads, moved to EB-2). S4: Morphic Tide and Glimpse of Tomorrow are now `MorphicTideEffect(PlayerSubject)`
(the shuffle in and the reveal of that many, recorded as REVEAL) then `PutCardsOntoBattlefieldEffect(revealedThisWay(...))` per batch
and `PutCardsIntoLibraryEffect.onBottom[InRandomOrder](revealedThisWay())`, so the Auras enter in one batch with the other
enchantments and enchant only what is already there (the Morphic Tide ruling, CR 303.4f); `MorphicTideAuraEffect`,
`MorphicTideBottomCardsEffect` and their handlers deleted (ledger rows added, ratchet 2857 / 2241). S7: Unfinished Business returns
the creature, then its attachments as one entry batch, two events (CR 608.2c, 603.2c; Tormod pins it); the entry batch now raises
the Equipment-attached event for an Equipment entering attached. O35: an entry batch asks its members' as-enters choices one after
the other (`BattlefieldEntryBatchSupport.continueAsEntersChoices`, `GameData.pendingEntryBatchAsEntersIds`, resumed by the input
epilogue), the "one or more enter" event open until all are done (CR 614.12). O3: Lim-Dûl's returned Aura card goes through the
entry batch (CR 303.4f, 303.4g); its bestow part is no defect (a bestow card is an Aura only when cast bestowed, CR 702.103b).
Dropped: T19 (Zara's put already gets every entry replacement from `place()`, a recruited Clone pinned), O2 (Painter's Servant fixed
in D19, pinned by Bringer of the Last Gift; Knickknack Ouphe fixed in fold run 7; Karlach's entry has no observable difference, its
perpetual "can't block" moved to PP-1 as N15), P4's Tevesh Szat part (the -10 is `PutCardsOntoBattlefieldEffect` through the entry
batch since fold run 4). New lines N14 (EB-2), N15 (PP-1). Tests: 683 XML files, 6,438 tests, 0 failures (546 card and application
classes with the architecture tests, the AI module).

#### EB-2. "As it would exist on the battlefield"

Depends on: EB-1.
Lines: L10 (`snapshotEnterTappedTypes` / `applyEnterTappedEffects` match printed types), L17 (`EnterPermanentsOfTypesTappedEffect.matching`
judges the permanent as it is), L7 (the returned-permanent animations of Brilliance Unleashed, Tune Up and The War in Heaven apply
after the permanent enters), O4 (Bad Wolf Bay's chaos ability; `CardsCantEnterBattlefieldFromZonesEffect` enforces only graveyard
and library).

Avoid: TK-1.

Moved from EB-1: O3's rest (`ReturnDyingCreatureToBattlefieldEffect`, `ReturnDyingCreatureToOwnerBattlefieldEffect` and
`ReturnDamagedCreaturesThatDiedUnderControlEffect` put an Aura card that died animated onto the battlefield with no host choice,
CR 303.4f, 303.4g; route them through the entry batch like Lim-Dûl's `ReturnDyingOpponentCreatureUnderYourControlEffectHandler`).

New line N14 (seen in EB-1): `GraveyardReturnSupport`'s mass returns (`handleCreatureEtbAndLegendRule` over `deferredEtbs`) ask each
returned permanent's as-enters choices in a loop that doesn't wait for an earlier one's open prompt, the O35 shape outside the
entry batch (CR 614.12); unverified.

Outcome: 2026-11-27. Fixed: L17, L7, O4, N14 and O3's two single-card heads (deleted from entry 18; O3 edited down to Krovikan
Vampire). L17: an enters-tapped (and enters-untapped) filter is judged inside the entering-permanent layer pass
(`BattlefieldPlacementService.entersMatching`, CR 614.12): Phyrexian Censor no longer taps a creature Xenograft makes Phyrexian.
L7: CR 611.2e: "is/it's [characteristic]" applies as it enters, "becomes" after. Brilliance Unleashed is two returns gated by the
new `TargetGraveyardCardMatches(CardPredicate, description)`, the noncreature one with Abuelo-style pre-entry
`battlefieldEffectGrants` (False Floor now taps the returned Sol Ring); The War in Heaven uses `withEntryAnimation` (Root Maze taps
its creatures); Tune Up ("becomes") is no defect, pinned. `AnimateReturnedPermanentIfNotCreatureEffect` and its handler deleted
(ledger row, ratchet 2856 / 2240). O4: Bad Wolf Bay floats `CardsCantEnterBattlefieldFromZonesEffect(null, {EXILE})` for the turn
(Oracle: "cards can't enter from exile this turn"); `isCardBlockedFromEnteringFromZone` reads floated ones and placement keeps a card
arriving from exile in exile (`keepCardThatCantEnterFromExile`). O3: `ReturnDyingCreatureToOwnerBattlefieldEffect` and
`ReturnDyingCreatureToBattlefieldEffect` send an Aura card through the entry batch with its riders (CR 303.4f, 303.4g; Luminous
Broodmoth, Nim Deathmantle). N14: the three `deferredEtbs` mass returns ask as-enters choices in turn through
`BattlefieldEntryBatchSupport.askAsEntersChoicesInTurn` (Rise of the Dark Realms, two Adaptive Automatons). Dropped: L10 (fixed in
D18 by `entersWithCardType`, pinned by False Floor's March test). Left: O3's Krovikan Vampire head (a mass return with control and
sacrifice links; needs the whole return moved onto the entry batch). New lines N27 (EB-3), N28, N31 (MC-2), N29 (LY-2), N30 (LG-1).
Tests: 1,627 XML files, 14,659 tests, 3 failures: `ReturnCardFromGraveyardEffectHandlerTest` (missing mock, fixed and rerun green
with the architecture tests, 11 files, 39 tests) and two `TergridGodOfFrightTest` cases that fail on HEAD too (N31).


#### EB-3. Leave batches and departures

Depends on: EB-2.
Lines: O107 (`ExileTargetPermanentEffectHandler`: several plain targets leave one by one, and non-set subjects get no single
event), O106 (Sarulf's exile), S6 (Kairi, Leadership Vacuum, Resounding Wave return one at a time), O78 (Bull Elephant,
Ovinomancer), O27 (Reality Scramble: `PutTargetOnBottomOfLibraryEffect` records no `DepartureResult`), S19
(`PutExiledCardsIntoOwnersGraveyardEffect` gives no graveyard-order prompt), O75 (Acererak's tokens and sacrifices).

Notes: For O78 and O75, first show that the difference is observable (a look-back trigger, a state check). If it isn't, drop
the line with the reason.

New line N27 (seen in EB-2): `GraveyardReturnSupport`'s other returns that place several cards themselves and then loop
`handleCreatureEtbAndLegendRule` over them (the steal-from-graveyards and exile-and-graveyard returns) ask each permanent's
as-enters choices without waiting for an earlier one's open prompt, the N14 shape (CR 614.12); unverified.

Outcome: 2026-11-27. Fixed: O107, O106, S6, O78, O27, N27 (deleted from entry 18). New shared step
`PermanentRemovalService.removeTogether(gameData, permanents, removal)`: two or more leave as one leave batch, each judged as all
were before any left (`performSimultaneousRemovals`; CR 603.2c, 603.10a, 608.2h). O107: `ExileTargetPermanentEffectHandler`'s
target path takes every target's last-known values first and exiles them together, and its non-set several-permanent subjects
get the snapshot too (Ulamog, Silence the Believers: Dour Port-Mage now sees Mind Stone leave as a creature under March of the
Machines / Animate Artifact). O106: Sarulf's exile through `removeTogether`. S6: Kairi's return
(`ReturnTargetPermanentsWithinTotalManaValueEffectHandler`) and Leadership Vacuum's commanders through `removeTogether`; Resounding
Wave was already dropped in D13. O78: a ransom's returned permanents (`ResolutionCostPaymentService.returnToHandTogether`, Bull
Elephant, Ovinomancer) leave as one event: Nefarious Imp triggered twice, now once. O27: Reality Scramble is
`PutTargetOnBottomOfLibraryEffect` then `RevealUntilCardPredicateRestOnBottomRandomEffect(CardSharesCardTypeWithPermanentsPredicate(
Subjects.targetDeclaredElsewhere()), BATTLEFIELD)`: the found card enters through the entry batch, so an Aura card gets a host
(CR 303.4f; it was lost before), with the target's last-known types (its ruling); the bespoke effect and handler deleted (ledger
row, ratchet 2855 / 2239; the AI keeps it NEUTRAL by `TargetPolarityClassifier.tradesTheTargetForOneSharingItsType`). N27: the
simultaneous puts of `GraveyardReturnSupport` (`putCardsOntoBattlefieldSimultaneously`, `...UnderController`,
`putCardsFromGraveyardAndExileOntoBattlefieldSimultaneously`, the Aura-retether placement, the pile placement) ask as-enters
choices in turn through `askAsEntersChoicesInTurn` with their "one or more enter" window (Patriarch's Bidding with two Adaptive
Automatons). Dropped: O75 (Acererak: no player acts and no trigger in the pool tells "sacrifices, then tokens" from simultaneous;
death triggers look back before both, entry triggers after both). Left: S19 as an owner question (entry 17: CR 404.3 ordering is
asked nowhere but the exchange; Dakra Mystic's part is no defect, one card per graveyard; the line edited down to Covenant of
Minds). New line N36 (MC-2). Tests: 741 XML files, 6,755 tests, 2 failures in the Mockito `ExileTargetPermanentEffectHandlerTest`
(the mocked `removeTogether` didn't run its removal; stubbed, rerun green with the architecture tests and
`PermanentRemovalServiceTest`, 18 files, 73 tests).

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

Outcome: 2026-11-27. Fixed: P22 and O73 (the one permanent a sacrifice ransom paid at once could take is sacrificed unasked,
CR 608.2d, unless its move to the graveyard has a death replacement question, whose pick still asks it first, CR 616.1; it is
recorded on the entry through the new `DestructionSupport.recordSacrificedOn`, so Three Dog's reflexive copy still reads it), P23
(`TapMultiplePermanentsCost.withDescription`: "taps an untapped creature", "another" for a source-excluding tap; 15 tap-ransom
cards), P24 (an X chosen while paying asks a Lich's Duel Mastery sacrifice's death replacements before its {B} is paid with life
under K'rrik, CR 119.4, 616.1: `Payment.replay`, an `InteractionAnswerReplay` of the X answer, `nextStep` waits as `PAYING`), O60
(Tariff, Winter's Chill, Fade Away, Tidal Flats, Heroism, Power Leak / Errant Minion and the per-creature untap of Magnetic
Mountain, Thelon's Curse, Mudslide and Dream Tides pay through `payManaFromPool` with the asking effect's controller as the taxing
player; `MultiPermanentChoiceContext.PayManaPerCreatureUntap.taxingPlayerId`; edited down to Reflect, latent). Deleted from entry 18:
P22, P23, P24, O73. Dropped (tests pass on HEAD, kept as pins): O59 (a K'rrik payer with only untapped Swamps is asked and pays,
`couldPossiblyPayMana` asks anyone with a mana source), O64 (likewise for ward—waterbend), O69 (Ice Cave: {G/P} paid with 2 life,
{R/W} with either colour; CR 107.4e, 107.4f), O70 (the pay path already reads "an Aura"; the paid-together `choose` prompt now uses
the article too); Forgotten Lore's payment is its own controller's, never a tax. Left: O71 (latent, not cheap), O72 (already in
entry 17 as U3b Q5; Tax Taker's text "pays a tax from a spell or permanent you control" doesn't define "tax"), O76 (latent: no card
has both), P25 (waits on the owner). `answerForcedPick` no longer throws when the one-permanent sacrifice was made unasked. No effect
class added or deleted, so no ledger or ratchet change; no `CONVERSION_VERSION` change.

#### PY-2. Mana: spending and producing

Depends on: PY-1.
Lines: P9 and P16 ("as though it were mana of any color" and "mana of any type" applied as the wrong thing, so coloured mana
can't pay {C}), P2 (Feather's {2} per creature is paid only from the pool), P15 (`getActivatedAbilityAdditionalGenericCost` with
a {0} equip or power-up), O25 (Caged Sun misses land mana abilities without {T}), O26 (`TappedLandManaSupport.producedManaTypes`'s
fallback adds mana for a tap that reported none), O37 (an `AwardAnyColorManaEffect` of amount 0 counts every colour), L14 (Kalain
and Coin of Mastery read cast-time mana snapshots by card id), P4's Charmed Pendant and Thran Portal parts.

Outcome: 2026-11-27. Fixed: P9 and P16 (one any-mana boolean served both wordings: a permission now carries `AnyManaSpending`,
`ANY_TYPE` paying {C} with any mana, CR 118.14, `AS_THOUGH_ANY_COLOR` leaving {C} to colorless mana, CR 107.4c; read by
`CastingPermissionService.anyManaSpending`, paid by the exile, graveyard and library-top paths; `.withManaAsThoughAnyColor()` on
`ExileTopCardMayPlayThisTurnEffect`, `AllowPlayCardsFromExileEffect`, `AllowCastFromCardsExiledWithSourceEffect`,
`ExileNonlandCardFromTargetHandOrGraveyardEffect`; Whispersteel's rider became `spendingManaAsThoughAnyColor()`, Hurl Through
Hell's `manaAsThoughAnyColor`, Siphon Insight's, Opposition Agent's and Share the Spoils' grants any color; 25 any-color card classes edited), P2 (Feather's
choice is bounded by the mana its controller could make and mana abilities may be activated while it asks, CR 605.3a), P15
(activation reductions lower increases too, also under a {0} paid rather than the cost: CR 601.2f, 118.9d, 602.2b; Bruenor +
Suppression Field + Nahiri, Fighter Class + Suppression Field), O37 (`ManaSourceColorSupport` skips an any-color ability that
would add no mana, CR 106.7; reuses `LandManaTypeSupport.producesAnyMana`), L14 (a permanent entering uncast clears its card's
Treasure and artifact cast snapshots, CR 400.7: a countered Kalain creature reanimated got its old counters). Dropped: L14's copy
half (a copy is a fresh card), O26 (every land-tap event reports the pool difference; no tap reaches the fallback), P4's Charmed
Pendant (fixed in D17, pinned) and Thran Portal (non-mana abilities cost no life; pinned with a chosen type). Left: O25 (Caged Sun
needs a "a land's ability adds mana" event beside `LandTappedForMana`, a trigger key, the collector and `PotentialManaService`'s
estimate; Calciform Pools and Rain of Filth are real cases). P4 edited down to Animation Module (PY-3). New lines N37 (CP-1), N38,
N39 (MC-2). No effect class added or deleted (`AnyManaSpending` is a model enum), so no ledger or ratchet change; no
`CONVERSION_VERSION` change.

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

Outcome: 2026-11-27. Fixed: F9, O39, O51, O56, O95, N8 (deleted from entry 18). Dropped: S10, not a defect: a doubled
trigger's extra instance is asked its own mode and target, because the stack door queues one prompt per instance
(`GameData.queueInteraction`), and the `TriggerAwareStackList.add` copy only clones an entry that chose nothing (pinned in
`PanharmoniconTest`: Flametongue Kavu with two targets, Tax Collector with two Arrest modes; CR 603.2d, 603.3c, 603.3d). F9: the
simultaneous-death scan skips a face-down Hushbringer (CR 708.2a). O39: the ally combat-damage collector judges an event
condition against the player dealt the damage, and the 14 implemented "deals combat damage to an opponent" cards that lacked
it get `AttackedTargetIsOpponent` (Coastal Piracy, Strixhaven Stadium, Kediss, Parapet Thrasher, Hydra Omnivore, Amarant
Coral, Mindscour Dragon, Rampaging Raptor, Super State, Questing Beast, Black Widow Intel Expert, Vincent's Chaos, Kosei's and
Human Torch's granted ones), so combat damage redirected to their controller (Reflect Damage, CR 614.9) doesn't trigger them.
O51: Voracious Brood is one "one or more from anywhere" trigger (CR 603.2c). O56: `EtbTriggerService` names the entering
permanent as the triggering permanent of a "this or another [X] enters" ability on its own entry
(`TriggeredAbility.alsoWatchesAnotherEntering`); no shipped card read it wrongly, since Scourge of Valkas and Hawkeye, Trick
Shot fall back to the source. O95: the "target creature gets" order label needs a target subject. N8: "you get {E}" goes
through `LifeSupport.givePlayerCounters`, so All Will Be One triggers. No effect class added or deleted, so no ledger or
ratchet change.


#### TR-2. Subjects and last-known information

Depends on: TR-1.
Lines: T2 (`CardSubject.TriggeringCards` reads no type for a card that left the zone its event put it in), T10
(`SubjectResolutionService.attachedId` with no source snapshot), O10 (`TriggeringPermanentPower` / `Toughness` prefer the
trigger-time value), O40 (Terminal Velocity's then-steps over `Subjects.source()`), E12 (`handleLookAtTopCardPutLandOrCreatureChoice`'s
Aura put records no RETURN step), S11 (`ParleyEffect`'s token form creates tokens inside its handler).

Likely touches: `SubjectResolutionService`, `DynamicAmount` readers, `MayMiscHandlerService`.
Avoid: FD-2.

Outcome: 2026-11-27. Fixed: T2, O10, E12 (deleted from entry 18). Dropped: T10, O40, S11. T2: `bindCardSubjects` reads the
cards a `triggeringCards()` subject names as they last existed once they left the zone the event put them in (CR 608.2h), so
"shares a card type with one of the discarded cards" still reads a discarded card exiled instead or moved since; Diviner of
Fates folds onto `SeekCardsToHandEffect(Fixed(1), CardSharesCardTypeWithCardsPredicate(triggeringCards()))` and
`SeekCardSharingCardTypeWithDiscardedCardsEffect` and its handler went (ledger row; ratchet 2854 records, 2238 handlers). O10:
a departing permanent's last-known power and toughness on the entries that target it or were triggered by it (and
`TriggeringPermanentPower`'s refreshed value) are its `lastKnownPower` / `lastKnownToughness`, judged with what left in the same
event still applying, not its power after an earlier member of the batch was already gone (Terror of the Peaks with a Benalish
Marshal leaving alongside, CR 608.2h, 603.10a); the resolving-entry gap is N40. E12: the top-card put's Aura branch goes through
the entry batch with a RETURN record (CR 303.4f), so Rulik's "if you don't put a card onto the battlefield" reads a put land
Aura; the dead Maelstrom-era `handleRevealedPermanentPutOntoBattlefieldOrBottomChoice`, `handleRevealTopCardMayBottomChoice`
and the three-argument overload went. T10: not a defect: `PermanentRemovalService` gives every entry whose source leaves a
source snapshot, also when the Aura and its host leave as one event (pinned in `SubjectResolutionServiceTest`). O40: not a
defect: the then-steps run with the put permanent as the entry's target, and `TerminalVelocityTest` already shows haste and
both granted triggers on it. S11: already fixed by defect run D14 (the token form queues the tokens and the draws as later
steps), pinned in `InnocuousResearcherTest` with Esix. New line N40 (MC-2).

#### TR-3. Target and choice prompts

Depends on: TR-2.
Lines: T5 (`beginAnyTargetChoice` and the permanent-or-player prompt don't drop what can't be chosen), T6 (a name prompt that
requires a type uses a copy's types), T20 (Descendants' Fury offers creatures that can't be sacrificed), T17
(`ExileGraveyardCardsEffect.defendingPlayerOnly` is never read), T22 (Rasaad's specialize target spec), T3 (Soundwave's "from their
graveyard"), O29 (`CounterSpellEffect` with several targets and `LIBRARY_TOP_OR_BOTTOM` opens overlapping prompts), O31 (Bow of
Nylea and The Moonbase: the activation path needs the `targetGraveyardCards` branch the cast path got in fold run 50c).

Outcome: 2026-11-27. Fixed: T20, T6 (deleted from entry 18). Dropped: T17, T22, T3, O29, O31. Left: T5 (edited down in entry
18). T20: "you may sacrifice one of them" with none of them sacrificeable isn't asked (CR 608.2d's own example: a player who
can't sacrifice can't choose that option); `MayEffectHandler` asks `ChoosePermanentsEffectHandler.couldSacrificeAny` for a
"may" led by a `toSacrifice()` choice (Descendants' Fury, two dealers under Assault Suits; the old lone-dealer test that
answered the "may" was rewritten). T6: a name prompt judges a face-up permanent by its own cards (its physical cards, and the
face it shows only when that face is theirs), so a Phyrexian Metamorph copying Grizzly Bears doesn't make "Grizzly Bears" an
artifact card name for Dispossess (CR 201.4a); `isNameExcludedByType` reads the same cards. T17: not a defect: the flag is read
through `choosesFromEventPlayersGraveyard` (`TriggeredAbilityStackService.graveyardOwner`), pinned three-player in
`GravenAbominationTest`. T22: not a defect: the trigger collector runs the effect's validator, which requires a card exiled with
this permanent; pinned in `RasaadMonkOfSelNeTest` (another exiled creature card isn't offered; with none, no prompt). T3: not a
defect: `graveyardOwner` narrows the opponent-graveyard target to the damaged player; pinned three-player in
`SoundwaveSonicSpyTest`. O29: no implemented card combines several counter targets with `LIBRARY_TOP_OR_BOTTOM` (Hinder has one
target), so nothing reaches it. O31: a fold prerequisite, not a rules defect: moved onto the Bow of Nylea and The Moonbase rows
of entry 16. T5: the prompts drop "can't be chosen" since D15; the sweep of about 415 callers that count or auto-pick before
prompting is left (only Glimpse the Unthinkable, a playtest card, has the ability). No effect class added or deleted, so no
ledger or ratchet change.

### LS. Library searches and reveals

#### LS-1. Whether to search, and the shuffle

Lines: E2 (may-searches have no "do you search?"), E3 (a same-name search with no match asks nothing; Haunting Echoes with nothing
exiled), E9 (a prevented search still shuffles), E10 (Last Light of Durin's Day always searches the library), E15 (Journey for the
Elixir doesn't shuffle after taking both cards from the graveyard).

Likely touches: `LibraryChoiceHandlerService`, `SearchLibraryAndOrGraveyardForCardToHandEffect`'s handler.
Avoid: FC-1, FC-2.

Outcome: 2026-11-27. Fixed: E2, E3, E9, E15 (deleted from entry 18). Left: E10, edited in entry 18 to its general form.
E2: an optional search of the basic-land queue (New Frontiers, Natural Balance, Weird Harvest, Veteran Explorer, From the
Ashes, ... 13 cards) and Settle the Wreckage first ask `PendingInteraction.MaySearchLibraryChoice` ("do you search?"): no is
not searching (no shuffle, Boldwyr Heavyweights ruling), yes then declining the pick is searching and finding nothing, so the
library is shuffled and search triggers fire (CR 701.23a, 701.23b). Settle the Wreckage no longer shuffles when the search is
prevented, nor searches automatically an empty library or one with no basic lands (Leonin Arbiter / Mindlock Orb ruling: "You
may search … then shuffle", you can't choose to search, so you won't shuffle). `LibrarySearchParams.declineSkipsShuffle`
(Settle's only setter) is deleted. E3: a same-name search for a named card that searches a library or hand with no match still
shows the searched zones (`MultiZoneExileChoice` with no choosable card, CR 701.23a); Haunting Echoes with nothing exiled is
dropped, not a defect: "for each card exiled this way, search" makes no search. E9: an "and/or" search with "if you search your
library this way, shuffle" doesn't shuffle a library that couldn't be searched, on a fruitless search or a decline (the same
rulings). E15: Journey for the Elixir is the two searches unshuffled plus `ShuffleLibraryEffect(false)`, so "then shuffle"
shuffles after two graveyard finds and under Leonin Arbiter. E10 left (too big for this run): every "and/or" search needs the
zones chosen before the pool is shown, which changes about 48 cards' tests. About 125 test methods in 38 card test classes
answer the new prompts. No effect class added or deleted, so no ledger or ratchet change.

#### LS-2. What a search and a draft record

Depends on: LS-1.
Lines: E7 (the library-search flows handled before the generic path record no found cards in SEARCH), E8 and H2 (the
`LibraryRevealChoice` drafts record no CONJURE step; Support Skyforge exiles the cards it didn't choose face up and names them),
O32 (Panglacial Wurm can be cast only on the first pick of a multi-pick search), T4 (Opposition Agent's player can take a cast-only
card; the AI's search strategy answers only for the searcher).

Avoid: FC-1, FC-2.

New line N35 (seen in LS-1): Journey for the Elixir's "search your library and graveyard" records a library search per
sub-search (twice, or never when both cards come from the graveyard), though the library is searched once (CR 701.23h).

E10 (left by LS-1, see its Outcome): every "and/or" search (Last Light of Durin's Day's hand and/or library, about 48
library-and/or-graveyard cards) chooses its zones before the pool is shown, so a pick of nothing isn't a library search and a pick
from another zone is.

Outcome: 2026-11-27. Fixed: E7, E8, H2, T4 (deleted from entry 18). Dropped: O32. Left: N35, E10. E7: the subtype and colour
picks (Gaea's Balance, Lotuslight Dancers, Conflux, ...) and Druid of the Emerald Grove's basic-land picks now add each found
card to the SEARCH outcome (a colour pick into the graveyard with that zone, CR 608.2c); the same-name, basic-land-queue and
`HAND_OR_GRAVEYARD` searches already recorded (fold run 13b, the generic path), and the held-out pools (Selective Adaptation,
the two-type looks) are looks, not searches. E8/H2: Adaptive Armorer and Masterpiece Vault draft through the plain
`SpellbookDraftChoice`, which records CONJURE and queues their `LibrarySelectionFollowUp` (`queueLibrarySelectionFollowUp`, now
public); Support Skyforge is the plain draft then `PerpetuallyGrantCardCharacteristicsEffect(artifact, creature)` over
`cardsThisWay(CONJURE)` ("becomes an artifact creature" keeps the other types, CR 205.1b). The cards not drafted go nowhere:
they were exiled face up and named (Armorer's and Vault's stayed in exile). Dragon Typhoon already drafted through the plain
prompt. `PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect` and its handler and `makeSelectedArtifactCreature` are
deleted (ledger row, ratchet 2853 / 2237). On the way: a permanent drafted onto the battlefield runs its entry abilities
whatever its type, as the entry batch does (only a creature's did). T4: `LibrarySearchAiStrategy` answers for
`decidingPlayerId()`, so an AI that controls Opposition Agent chooses an opponent's find (the Agent's cast-only half was fixed
in D15). O32 dropped, not a defect: the Panglacial Wurm ruling "If you want to cast Panglacial Wurm while searching your
library, you must do so before you find any cards with the search effect". N35 and E10 left, one change: an "and" search
(Journey for the Elixir) searches the library whatever is found and, with its second sub-search, only once (CR 701.23h); an
"and/or" one chooses its zones first. Both need the zones chosen on `SearchLibraryAndOrGraveyardChoice` (about 48 cards'
tests) and a once-per-resolution search record. No owner question.

#### LS-3. "And/or" searches choose their zones

Depends on: LS-2.
Lines: E10 and N35, left by LS-1 and LS-2 (read both Outcomes): a zone choice on `SearchLibraryAndOrGraveyardChoice` before the
pool is shown (CR 701.23a; about 48 cards' tests answer the new prompt), and a library search recorded once per resolution for an
"and" search (Journey for the Elixir; CR 701.23h). Also N41 and N42 if they fall out of the same change.

Outcome:

### TK. Tokens and copies

#### TK-1. Token replacements

Lines: S15 (`TokenKindReplacements` and `JinnieFayTokenReplacements` read `event.controllerId()`'s battlefield), S16 and L5 (the
two fixed-order `getTokenMultiplier` callers: `CreateTokensAttachedToControlledCreaturesEffectHandler` and
`ReturnTargetCreatureToHandAndCreateTokenCopyEffectHandler`), L3 (Life of the Party and Wedding Ring offer no first-token-creation
replacement), L4 (a token copy of a source reads `cardToCopy`, not `copiableCard`), O68 (Timothar: only the last Bat is linked).

Avoid: EB-1, EB-2.
Notes: L2 (Esix's instructions) is the owner's. Don't change the rider exclusions T1 kept.

Outcome: 2026-11-27. Fixed (deleted from entry 18): S15, S16, L3, L4, L5, O68. S15: whose token-creation replacements apply is
judged after Crafty Cutpurse but before Gather Specimens (`tokenCreationReplacements` resolves the creator without the
entering-control change; the Chatterfang ruling), and `TokenKindReplacements` / `JinnieFayTokenReplacements` read that creator's
battlefield, Jinnie Fay asking that player. S16: Supplant Form's copy goes through `TokenCopySupport` (one ordered event; a
Chatterfang Squirrel now comes with it); `getTokenMultiplier` is gone. L5 and S16's other half: Asinine Antics and Twisted
Sewer-Witch create through the ordered event and insert the new internal step `AttachCreatedTokensToHostsEffect`, which attaches
the Roles once the creation finished, a doubler's to the same creature (Asinine Antics ruling). L4: `cardToCopy` returns the
copiable values (`copiableCard`'s logic, CR 707.2), so every token-copy and duplicate reader skips a became-a-creature, bestowed
or Licid form (Supplant Form on an animated Veiled Serpent makes an enchantment). L3: Wedding Ring's target and Life of the
Party's opponents are offered their own Mirrormind Crown / Moonlit Meditation / Esix (`replaceFirstTokenCreation` with a
creator, `PendingTokenCreationReplacement.creatorId`; CR 111.2, the line's 701.7a doesn't say who creates). O68: the new internal
step `LinkCreatedTokensToExiledCardsEffect` links every Bat the creation made (`Permanent.linkedExileSourceId`, read by
`exiledWithSource`). New owner question in entry 17 (Roles under Halving Season); new lines N43 to N47 (TK-2). Ratchet 2853/2237
to 2855/2239, two ledger rows.

#### TK-2. Copies of spells and permanents

Depends on: TK-1.
Lines: S18 (Precursor Golem, Ink-Treader Nephilim: the copies' controller doesn't choose the stack order), T11 (Kefnet, Reversal
of Fortune, Spellweaver Volute: the copy is cast in exile, not in the card's zone), T12 (The Clone Saga chapter II offers new
targets), O99 (`CloneService.exileCopiedCardAbility` exiles a card that left the graveyard and came back), T1
(`ChoosePermanentsEffect` keeps a lone chosen permanent as the source's chosen permanent).

Avoid: DM-2.

New line N43 (seen in TK-1): Timothar, Baron of Bats puts the Bat's "When this token deals combat damage to a player, sacrifice
it and return the exiled card" on the token's blueprint, so a copy of a Bat has it and sacrifices itself; the ruling says a copy
won't have the ability Timothar grants ("It gains"). `TimotharBaronOfBatsTest` "A copy of the Bat returns nothing" pins the
sacrifice.

New line N44 (seen in TK-1): `CreateTokenAttachedToTargetEffectHandler` and `CreateTokensAndAttachEquipmentEffectHandler` attach
nothing when their token creation stops for the replacement order (CR 616.1): `applyCreateToken` returns no ids then. The
fix TK-1 used for Asinine Antics is an inserted `AttachCreatedTokensToHostsEffect` step; the per-target loop of the first one
also starts the next target's creation while the order prompt is open.

New line N45 (seen in TK-1): a token creation Crafty Cutpurse redirects is still ordered by the original creator
(`TokenCreationEvent.chooserId()` is `controllerId`), though the Cutpurse ruling makes the tokens' would-be controller choose
among the effects (CR 616.1). Jinnie Fay's choice already goes to the would-be controller since TK-1.

New line N46 (seen in TK-1): Roles a doubler creates attached to one creature get one timestamp each as they are attached, so
CR 704.5z keeps the last one; the Asinine Antics ruling says that player chooses which Role to keep when two or more become
attached at the same time.

New line N47 (seen in TK-1): Life of the Party with three or more players: when an opponent accepts Esix's replacement (only on
their own turn), Esix's creature choice ends the resolution, so the copies of the opponents after them are never created
(`CreateTokenCopyOfSourceForEachOtherPlayerEffectHandler`).

Outcome:

### FD. Face-down and hidden reads

#### FD-1. Colours and static-set reads

Lines: F8 (`getEffectiveCardColors`' off-battlefield scan asks only the floating flag), F12 (Painter's Servant doesn't colour
cards off the battlefield or spells), F11 (`layeredQueryReads`' `matchesLeaf` uses printed keywords and types).

Likely touches: `GameQueryService`.
Avoid: PP-1.

New line N1 (seen in DM-1): `GameQueryService.hasAuraWithEffect` reads an attachment's printed statics with no lost-ability
check, so the other Aura and Equipment prevention steps share F7's defect.

New line N34 (seen in TR-1): `GameQueryService.countAdditionalCreatureDeathEffects` (Teysa Karlov's "triggers an additional
time") skips a static source only on the lost-abilities flag, not a face-down one, on the battlefield and in the
simultaneous-death scan (CR 708.2a); unverified.

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

New line N29 (seen in EB-2): a return's keyword grants ("it's a 3/3 Robot artifact creature with flying", Brilliance Unleashed,
Abuelo's Awakening) apply after the permanent enters: `GraveyardReturnSupport`'s pre-entry `battlefieldEffectGrants` cover only
colour, card types, subtypes and base P/T, though "has [characteristic]" applies as it enters (CR 611.2e); unverified.

Outcome:

#### LY-3. Protection

Lines: L12 (`ProtectionFromColorsEffectHandler` gives a self-subject protection to the host when its source is attached), L13 (Mask
of Law and Grace, Shield of Duty and Reason, Strength of Isolation and Strength of Lunacy stay exempt from their own protection
when recoloured).

Avoid: DM-1.

New line N24 (seen in DM-3): `DamageSupport.isRedirectedToProtectedCreature` pre-checks a redirected event's protection from the
stack entry's source card, not from the event's `explicitSource`, so a fought creature's damage redirected to another creature
(Shaman en-Kor) is judged as the fight's source; the pool's `Protection` rule already reads the right source (unverified).

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
O39 was fixed in TR-1 (the `AttackedTargetIsOpponent` event condition on every "deals combat damage to an opponent" card).

Likely touches: `CombatBlockService`, `CombatAttackService`, `AttackLegalityService`, `ConditionEvaluationService`.
Notes: Test with three players.

New line N48 (seen in MP-2): `CombatAttackService.declareAttackers` raises the generic attack tax's tax-paid event (Ghostly
Prison, Windborn Muse) toward `getOpponentId`, not the attacked player whose permanent imposed it (CR 508.1h; matters with Tax
Taker); MP-2 made the Phyrexian part (Norn's Annex) per attacked player.

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

New line N15 (seen in EB-1): Karlach's graveyard specialize return (`SpecializeKarlachEffectHandler.returnSpecializedCardFromGraveyard`)
gives "perpetually can't block" as a static on a runtime copy of the card, so the card loses it when it changes zones (O2's
Karlach part; its entry outside the entry batch has no observable effect).

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

New line N16 (seen in PY-1): Heroism's "prevent all combat damage that would be dealt by that creature" is registered with the
attacking creature's controller as the prevention's controller (`PreventCombatDamageFromAttackingCreaturesUnlessPaysHandler` passes
`ability.controllerId()`), not Heroism's controller (`ability.sourceControllerId()`, set since PY-1).

New line N17 (seen in PY-1): a `SacrificeMultiplePermanentsCost` ransom paid at once (Valgavoth, `counterUnlessSacrifices(n, ...)`)
still asks the pick when exactly that many permanents could be sacrificed; the one-permanent case is made unasked since PY-1 (the
CR 608.2d convention), the multi one would need the same death-replacement-question guard.

New line N19 (seen in AI-2): the AI picks a triggered ability's permanent target from the card's ETB and spell effects
(`AiDecisionEngine.answerEtbTargetPrompt` sends an attack trigger's `AttackTriggerTarget` to `chooseTarget(card)`) or with
`PermanentChoiceAiStrategy`'s "opponent's strongest creature first", never from the trigger's own effects, so Ballroom
Brawlers, Inspirit, Manifold Mouse, Ketria and Shaper Parasite aim their modal triggers by the wrong polarity.

New line N20 (seen in AI-2): the hard AI's MCTS is offered blocker declarations the engine rejects (`GameSimulator`'s
`BlockerDeclaration` actions always include no block, and single blocks of a menace attacker), so under a block requirement
(a menace attacker wearing Slayer's Cleaver) it can pick "no block" and fall back after the refusal; the candidates aren't
checked against `CombatBlockService`'s validation (CR 509.1c).

New line N21 (seen in AI-2): `SpellEvaluator.drawScore` gives "each player draws" no value whatever the player count, though
in a multiplayer game the AI's opponents draw several sets to its one.

New line N22 (seen in AI-2): the hard AI's damage board sweep (`DealDamageEffect.sweepsBoard()`,
`SpellEvaluator.evaluateBoardWipeDamage`) ignores the set's filter, so "damage to each creature without flying" is scored
over every creature on both battlefields.

New line N23 (seen in AI-2): `TargetPolarityClassifier`'s `DistributeCountersAmongTargetsEffect` rule reads only +1/+1 and
-1/-1 counters, not the other power/toughness kinds AI-2 gave the put-counter rule.

New line N25 (seen in DM-3): the "have it dealt to you instead" choice (`DamageSupport.queueCreatureDamageRedirectChoice`, the
`AllCreatureDamageRedirectChoice` rule) builds its prompt and redirected damage from the stack entry's source, not the event's
`explicitSource`, so a fought creature's damage accepted this way is dealt as the fight's source's (unverified).

New line N26 (seen in FC-3): a graveyard cast through a flashback grant (`SpellCastingService` graveyard path) refuses a mana,
discard or sacrifice escalate or entwine payment ("not supported from this zone"; Blessed Alliance after Past in Flames), though
CR 702.120a and 702.42a make it an additional cost paid with the alternative cost (read in the code, not tested).

New line N28 (seen in EB-2): `ReturnDyingCreatureToBattlefieldEffectHandler` (Nim Deathmantle, Scythe of the Wretched,
Oathkeeper, Meathook Massacre II) never asks `isCardBlockedFromEnteringFromZone`, so a Grafdigger's Cage doesn't stop its creature
return; unverified.

N31 (seen in EB-2): fixed by the orchestrator after EB-2 (the two Tergrid tests no longer answer a pick PY-1 made unasked).

New line N33 (seen in UI-1): a `GraveyardChoice` over a card pool that holds a hidden (face-down exiled) card is projected without cards, so the
client still guesses the pool as every graveyard's creature and artifact cards and can show the wrong cards or none; the fix is a
face-down placeholder view per hidden pool card (`CardViewFactory`'s face-down view), then the client's fallback can go.

New line N36 (seen in EB-3): handlers that move several permanents with their own loop of single removals and no leave batch or
simultaneous snapshot, so a later one is judged after an earlier one left (the O107 shape, CR 603.10a, 608.2h), among them
`ExileTargetCreaturesUntilSourceLeavesEffectHandler`, `ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffectHandler`,
`ExileTokensCreatedWithSourceEffectHandler`, `ReturnTargetPermanentsThenEffectHandler` and `FlickerEffectHandler` (17 handlers by a
grep); route each through `PermanentRemovalService.removeTogether`; unverified.

New line N38 (seen in PY-2): the cast-time mana snapshots keyed by card id (`GameData.spellCastTreasureManaSpent` and the other
`spellCast*` maps) are cleared only when the spell resolves, and `add*` merges, so a spell that leaves the stack unresolved
(countered into its owner's hand, Remand) and is cast again reads both casts' mana (Kalain, Coin of Mastery, converge); clear them
as the card leaves the stack or as a new cast begins; unverified.

New line N39 (seen in PY-2): `StackResolutionService.resolvePlaneswalkerSpell` and the battle path never call
`Permanent.setCast`, so a planeswalker or battle that entered from its cast spell reads as not cast (`isCast()`) to "if it was
cast" readers; unverified.

O25 (Caged Sun, left by PY-2, see its Outcome): a land's mana ability without {T} (Calciform Pools, Rain of Filth) raises no
"a land's ability causes you to add" event; needs that event beside `LandTappedForMana`, a trigger key, `TapTriggers` and
`PotentialManaService`'s estimate.

New line N40 (seen in TR-2): `PermanentRemovalService` refreshes a departing triggering permanent's last-known power and
toughness on the stack entries, trigger records and the parked entry, but not on `gameData.resolvingEntry`, so a triggering
permanent that leaves while its own ability resolves, after a change since the trigger, keeps its trigger-time power for
`TriggeringPermanentPower` (CR 608.2h); no card found that changes and then removes it mid-resolution; unverified.

New line N41 (seen in LS-2): Nissa's Encouragement (`SearchLibraryAndGraveyardForNamedCardsToHandEffectHandler`) shuffles
without recording a library search when every name is found in the graveyard, and with an empty library logs "Library is
shuffled" but neither records nor shuffles, though "search your library and graveyard" searches the library (CR 701.23a).

New line N42 (seen in LS-2): a card Opposition Agent's controller takes from a search (`handleOppositionAgentChoice`, before
`recordFoundCard`) isn't in the search's SEARCH outcome, so a later step about "the card found" reads nothing; unverified
whether a card exiled instead counts as found.

New line N49 (seen in MP-2): Sheltering Ancient's cumulative upkeep (`PutCounterOnOpponentCreatureCost`:
`ResolutionCostPaymentService.opponentCreaturesForCounter`, `MultiPermanentChoiceHandlerService.handleOpponentCreatureCounterPlacement`)
offers only `getOpponentId`'s creatures; "a creature an opponent controls" is any opponent's, chosen per age counter (CR 702.24a).

New line N50 (seen in MP-2): a noncreature permanent with `ChooseOpponentOnEnterEffect` put onto the battlefield by an effect,
not by its resolving spell, may still get no opponent choice with three or more players (MP-2 added it to
`StackResolutionService.processResolvedPermanentEtb`, the spell path); unverified.

New line N51 (seen in LG-1): `ClashSupport`'s outcome line says "No one won the clash." also when the other player's card had
the higher mana value, and `performClash` still reads the opponent as the first other seat in turn order (multiplayer).

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

Outcome: 2026-11-27. Fixed: O92, O103, O52, O108, O98, O102, T16, T21, N9 (deleted from entry 18). `BoostEffect`'s target
spec is harmful only when an amount lowers power or toughness (both target forms). `TargetPolarityClassifier` classifies the
two `ChooseOneFor…` records by their modes, reads every power/toughness counter kind (not only ±1/±1) and aims a branch
conditioned on "an opponent protects it" at the AI's own battle; the hard AI's `chooseAbilityTarget` and the simulator's
ability-target ranking now follow the classifier when it gives a direction, and a target filter that admits only the AI's own
permanents (Sigurd's "Saga you control") is aimed at its own board (O52's by-kind removal rule, the owner's 2026-11-25
decision, kept). `SpellEvaluator` scores a set an opponent controls (Simoon, "destroy all … target player controls") over
the opponents' battlefield only, "damage to each of up to N targets" per target, "each opponent draws" once per opponent,
Avatar's Wrath as a sweep sparing the AI's best creature (`AirbendTargetPermanentEffect.removalKind()` is EXILE only for a
target subject), and Wand of Ith's discard as the payer's lost card. A filtered "must be blocked by [X] if able" under menace
gets at least one matching blocker in the greedy pass, the exhaustive phase 1b and the Easy AI (CR 509.1c; Slayer's Cleaver
ruling). New lines N19 to N23 in MC-2. No effect class added or deleted, so no ledger or ratchet change.

### UI. Frontend

#### UI-1. Multi-permanent pick minimum

Lines: O17 (the client ignores `MULTI_PERMANENT_PICK`'s minimum and shows graveyards for an index pick over a pool;
`GameSimulator` answers a multi-pick with the first maximum).

Outcome: 2026-11-27. Fixed: O17's client half (deleted from entry 18). `GameChoiceService` kept `MULTI_PERMANENT_PICK`'s `minCount`
nowhere, so Confirm answered with fewer picks than the minimum; it now keeps it, guards `confirmMultiPermanentChoice` with
`canConfirmMultiPermanentChoice`, disables both Confirm buttons below it and shows "at least N" in the side prompt (first red:
`game-choice.service.spec.ts`, new, 3 specs). The graveyard index pick over a card pool: the client guessed the pool as every
graveyard's creature and artifact cards; the server now sends the pool (`InteractionPromptMessage.graveyardPoolIndexPick`, the
indices address it) when every card in it is in a graveyard, and the client shows exactly those (owner from the graveyard that
holds the card); a pool holding a card the server hides (face-down exile) is still sent without cards, as
`pooledIndexPickSendsNoCard` pins, and the client keeps its old guess for that case only (new line N33, in MC-2). Dropped: the
`GameSimulator` half (D20 stands: `limit(maxCount)` of the valid ids is a legal answer whenever a legal one exists; a rollout
fallback, not a rules defect). No effect class added or deleted, so no ledger or ratchet change; no `CONVERSION_VERSION` change.


### LG, CL. Logs and non-rules cleanup

#### LG-1. Log lines

Lines: H3 (`ChooseCardEffectHandler` logs a revealed card in hand unnamed; keep hidden-ness on the subject, since a zone check broke
Addle and Intuition before), H5 (`SetBasePowerToughnessEffectHandler` prints "null"), H6 (Shadow of Doubt logs nothing), H10
(`ClashSupport` doesn't name the player).

Avoid: CB-1 (H8 is there).

New line N2 (seen in DM-1): the Healing Grace shield's order-prompt label says "(Healing Grace)" for Errant Minion's shield too.

New line N18 (seen in PY-1): a tap ransom paid at once picks one permanent at a time with `MultiplePermanentTapCostHandler`'s
"Choose an untapped permanent to tap" whatever the cost's `description` (Raff Weatherlight Stalwart's creatures, Thousand Moons
Smithy's artifacts and/or creatures).

New line N30 (seen in EB-2): `ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler.applyEntryAnimation` logs "becomes a X/X
creature" for an animation that only adds a card type (The War in Heaven's "they're artifacts").

Outcome: done 2026-11-27, all of H3, H5, H6, H10, N2, N18 and N30 fixed, each with a test that failed first. H3: `ChooseCardEffectHandler`
names a hand card of a `cardsThisWay(REVEAL)` subject (Addle) and keeps every other hand choice unnamed. H5: a one-sided setter's
set, opponents' and player-target lines say the side it sets (no card reaches them; a synthetic card in
`SetBasePowerToughnessLogTest`). H6: `GrantStaticEffectToPlayerEffectHandler` logs "<card> affects <players> <duration>". H10:
`ClashSupport` names the opponent. N2: the target-and-source shields carry their card's name and the order prompt reads it
(`DamagePreventionService.targetSourcePreventionShieldName`; `PreventDamageToTargetFromSourceChoice` got a `cardName`). N18:
`MultiplePermanentTapCostHandler.getPromptMessage` reads the cost's description. N30: `AnimationSupport.animatePermanently` logs
a type-only animation as "becomes an artifact". New line N51. No `CONVERSION_VERSION` change.

#### CL-1. Dead code and a misnamed test

Lines: O46 (`RevealedHandChoice.bottomThenDrawMode` / `discardThenDrawMode` are dead), O100 (`ActivatedAbilityExecutionService`'s
inline mana-ability draw branch is likely dead), O96 (`BoostEffectHandler`'s snapshot refresh changes nothing), O104
(`TheGrimCaptainsLockerTest.doesNotGrantEscapeToNonCreatureCards` doesn't test what its name says).

New line N3 (seen in DM-1): unused fields left in `DamageSupport`, `MultiPermanentChoiceHandlerService` and
`PermanentChoiceBattlefieldHandlerService`.

New line N7 (seen in MP-1): about 100 card test classes carry their own copy of a private `addOpponent` / `addThirdPlayer`
helper; they can use `GameTestHarness.addOpponent(name)` instead (Abstract Performance's, Hostile Negotiations' and Threats
Undetected's older test methods also have no `@DisplayName`).

Outcome: 2026-11-27. Done (deleted from entry 18): O46 (`RevealedHandChoice`'s `bottomThenDrawMode` and `discardThenDrawMode`
components, their branches in `CardChoiceHandlerService`, and the 17 positional constructors and callers; no caller ever passed
`true`), O100 (the `DrawCardEffect` / `ManaAbilityCardDrawingEffect` branches of the inline mana-ability resolution; the marker
interface `ManaAbilityCardDrawingEffect` deleted, `DrawCardEffect` implements `CardDrawingEffect`; `drawService` and
`playerInteractionSupport` fields of `ActivatedAbilityExecutionService` went with it), O96 (`BoostEffectHandler`'s snapshot
refresh; a departing permanent refreshes every waiting entry itself in `PermanentRemovalService`), O104 (the test now holds a
Shock plus four other cards, mana for the escape cost, and resolves the escape cast through the engine). N3 done: unused fields
removed from `DamageSupport` (`permanentRemovalService`, `permanentControlSupport`), `MultiPermanentChoiceHandlerService`
(`waterbendPaymentService`, `animationSupport`, `librarySearchSupport`) and `PermanentChoiceBattlefieldHandlerService`
(`permanentControlSupport`, `triggerTargetCollector`, `predicateEvaluationService`, `lifeSupport`, `drawService`). N7 partly: the
`@DisplayName`s of Abstract Performance's, Hostile Negotiations' and Threats Undetected's older methods added (those three
already use `GameTestHarness.addOpponent`); the 135 card-test classes with a private `addOpponent` / `addThirdPlayer` copy are
left (over the 30-file limit; many copies differ from the harness's, e.g. a fixed connection name or no session registration).

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
