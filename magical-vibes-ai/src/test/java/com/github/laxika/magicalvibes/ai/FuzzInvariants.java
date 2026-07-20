package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Poll-loop game-state invariants for the AI fuzz tests. Checks run from outside the engine
 * between actions, so most use a two-strike rule: a poll can land between two engine steps of
 * a multi-part zone move, and a single observation may be a transient rather than a bug.
 *
 * <p>Each instance tracks the two-strike state for one game — create a fresh instance per
 * game and call {@link #check(GameData)} on every poll.</p>
 */
final class FuzzInvariants {

    private final GameQueryService gameQueryService;
    private final Map<UUID, String> initialCardNames;

    /** Violation observed on the previous poll, keyed by check name. */
    private final Map<String, String> previousViolations = new HashMap<>();

    /**
     * @param initialCardNames identity of every card the game started with, for the
     *                         conservation check: none of these may ever vanish or appear
     *                         in two zones at once
     */
    FuzzInvariants(GameQueryService gameQueryService, Map<UUID, String> initialCardNames) {
        this.gameQueryService = gameQueryService;
        this.initialCardNames = initialCardNames;
    }

    /**
     * Returns a violation description, or {@code null} if all invariants hold. Structural
     * violations (duplicate/corrupt permanents) fail immediately; everything else uses the
     * two-strike rule.
     */
    String check(GameData gd) {
        synchronized (gd) {
            String structural = findStructuralViolation(gd);
            if (structural != null) {
                return structural;
            }

            // Skip all other checks while the engine is holding cards aside for a pending
            // choice (e.g. "look at the top N") — they are legitimately outside every zone
            // at that moment, and a duplicate legend or zero-life state may be exactly what
            // the pending choice is about to resolve.
            if (gd.interaction.isAwaitingInput()) {
                return null;
            }

            String violation = confirm("conservation", findConservationViolation(gd));
            if (violation != null) {
                return violation;
            }
            violation = confirm("dangling-entry", findDanglingResolutionEntry(gd));
            if (violation != null) {
                return violation;
            }
            violation = confirm("token-zones", findTokenZoneViolation(gd));
            if (violation != null) {
                return violation;
            }

            // The remaining checks assert states that state-based actions must have cleaned
            // up, and SBAs only run when a player would receive priority — wait for a quiet
            // stack before claiming they failed.
            if (gd.stack.isEmpty()) {
                violation = confirm("sba-toughness", findSbaViolation(gd));
                if (violation != null) {
                    return violation;
                }
                violation = confirm("legend-rule", findLegendRuleViolation(gd));
                if (violation != null) {
                    return violation;
                }
                violation = confirm("attachment", findAttachmentViolation(gd));
                if (violation != null) {
                    return violation;
                }
                violation = confirm("game-over", findGameOverViolation(gd));
                if (violation != null) {
                    return violation;
                }
            }
        }
        return null;
    }

    /** Two-strike rule: report only when the identical violation was seen on the previous poll. */
    private String confirm(String checkKey, String violation) {
        String previous = previousViolations.put(checkKey, violation);
        return violation != null && violation.equals(previous) ? violation : null;
    }

    // ------------------------------------------------------------------
    // Immediate structural checks
    // ------------------------------------------------------------------

    private String findStructuralViolation(GameData gd) {
        Set<UUID> seenPermanentIds = new HashSet<>();
        for (UUID pid : gd.orderedPlayerIds) {
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (p.getCard() == null) {
                    return "invariant violated: permanent " + p.getId() + " has a null card";
                }
                if (!seenPermanentIds.add(p.getId())) {
                    return "invariant violated: permanent " + p.getCard().getName()
                            + " (" + p.getId() + ") appears on multiple battlefields";
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Two-strike checks
    // ------------------------------------------------------------------

    /**
     * Every card the game started with must appear exactly once across all zones:
     * libraries, hands, graveyards, battlefields, exile and spells on the stack.
     * Token and copy cards created mid-game are ignored (they may legitimately
     * cease to exist). Battlefields are counted via {@link Permanent#getOriginalCard()}
     * because that is the object the engine moves between zones (transformed
     * permanents carry a different face on {@code getCard()}).
     */
    private String findConservationViolation(GameData gd) {
        Map<UUID, Integer> counts = new HashMap<>();
        for (UUID pid : gd.orderedPlayerIds) {
            countCardIds(gd.playerDecks.get(pid), counts);
            countCardIds(gd.playerHands.get(pid), counts);
            countCardIds(gd.playerGraveyards.get(pid), counts);
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (p.getOriginalCard() != null) {
                    counts.merge(p.getOriginalCard().getId(), 1, Integer::sum);
                }
            }
        }
        for (ExiledCardEntry entry : gd.exiledCards) {
            counts.merge(entry.card().getId(), 1, Integer::sum);
        }
        for (StackEntry entry : gd.stack) {
            // Ability entries reference a source card that is already counted in its
            // own zone; copy entries were never part of the initial pool.
            if (entry.isCopy()
                    || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                    || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
                continue;
            }
            if (entry.getCard() != null) {
                counts.merge(entry.getCard().getId(), 1, Integer::sum);
            }
        }

        List<String> problems = new ArrayList<>();
        for (Map.Entry<UUID, String> expected : initialCardNames.entrySet()) {
            int count = counts.getOrDefault(expected.getKey(), 0);
            if (count != 1) {
                problems.add(expected.getValue() + " (" + expected.getKey() + ") found "
                        + count + " times across all zones");
            }
        }
        return problems.isEmpty() ? null
                : "card conservation violated: " + String.join("; ", problems);
    }

    private void countCardIds(List<Card> cards, Map<UUID, Integer> counts) {
        if (cards == null) {
            return;
        }
        for (Card c : cards) {
            counts.merge(c.getId(), 1, Integer::sum);
        }
    }

    /**
     * With no interaction active or queued, no stack entry may be parked in
     * {@link GameData#pendingEffectResolutionEntry}: completion handlers must either resume
     * the entry's resolution or clear it. A dangling entry silently truncates the spell's
     * remaining effects and can duplicate its card when a later flow resumes it against a
     * changed board (the Beacon of Unrest graveyard-choice bug).
     */
    private String findDanglingResolutionEntry(GameData gd) {
        if (gd.pendingEffectResolutionEntry == null || !gd.pendingInteractions.isEmpty()) {
            return null;
        }
        Card card = gd.pendingEffectResolutionEntry.getCard();
        return "dangling pendingEffectResolutionEntry: '"
                + (card != null ? card.getName() : "<no card>")
                + "' is parked mid-resolution but no interaction is active or queued";
    }

    /**
     * CR 704.5d — a token in a hand or library is a rules violation with gameplay impact
     * (hand size, discard and search interactions). Graveyards are deliberately not checked:
     * the engine keeps dead tokens in graveyard lists ("dies" triggers need the zone change)
     * and filters them at read sites instead.
     */
    private String findTokenZoneViolation(GameData gd) {
        List<String> problems = new ArrayList<>();
        for (UUID pid : gd.orderedPlayerIds) {
            appendTokenProblems(gd.playerHands.get(pid), "hand", problems);
            appendTokenProblems(gd.playerDecks.get(pid), "library", problems);
        }
        return problems.isEmpty() ? null
                : "token in a hidden zone: " + String.join("; ", problems);
    }

    private void appendTokenProblems(List<Card> cards, String zoneName, List<String> problems) {
        if (cards == null) {
            return;
        }
        for (Card c : cards) {
            if (c.isToken()) {
                problems.add("token card " + c.getName() + " (" + c.getId() + ") is in a " + zoneName);
            }
        }
    }

    /**
     * With an empty stack and no pending input, state-based actions must have
     * finished: no creature with toughness &le; 0 may still be on a battlefield.
     * (Toughness only — lethal-damage SBAs are excluded because indestructible
     * and regeneration make them unreliable to verify from outside the engine.)
     */
    private String findSbaViolation(GameData gd) {
        List<String> problems = new ArrayList<>();
        for (UUID pid : gd.orderedPlayerIds) {
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (gameQueryService.isCreature(gd, p)) {
                    int toughness = gameQueryService.getEffectiveToughness(gd, p);
                    if (toughness <= 0) {
                        problems.add(p.getCard().getName() + " (" + p.getId() + ") has toughness "
                                + toughness + " but survived state-based actions");
                    }
                }
            }
        }
        return problems.isEmpty() ? null : "SBA violation: " + String.join("; ", problems);
    }

    /**
     * CR 704.5j — with an empty stack and no pending input, no player may control two or
     * more legendary permanents with the same name. Mirrors {@code LegendRuleService},
     * including supertypes granted by static effects (e.g. In Bolas's Clutches).
     */
    private String findLegendRuleViolation(GameData gd) {
        List<String> problems = new ArrayList<>();
        for (UUID pid : gd.orderedPlayerIds) {
            Map<String, Integer> legendaryNameCounts = new HashMap<>();
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                if (isLegendary(gd, p)) {
                    legendaryNameCounts.merge(p.getCard().getName(), 1, Integer::sum);
                }
            }
            for (Map.Entry<String, Integer> e : legendaryNameCounts.entrySet()) {
                if (e.getValue() >= 2) {
                    problems.add("player " + pid + " controls " + e.getValue()
                            + " legendary permanents named " + e.getKey());
                }
            }
        }
        return problems.isEmpty() ? null : "legend rule violated: " + String.join("; ", problems);
    }

    private boolean isLegendary(GameData gd, Permanent p) {
        return p.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)
                || gameQueryService.computeStaticBonus(gd, p)
                        .grantedSupertypes().contains(CardSupertype.LEGENDARY);
    }

    /**
     * CR 704.5m/n/q — with an empty stack and no pending input, every aura must be attached
     * to an existing permanent or player, and attached equipment's host must exist.
     * Attachment <em>legality</em> (enchant restrictions, protection) is deliberately not
     * re-checked here — that would duplicate {@code AuraAttachmentService} and drift; this
     * only catches attachments the SBA sweep lost track of entirely.
     */
    private String findAttachmentViolation(GameData gd) {
        Set<UUID> permanentIds = new HashSet<>();
        for (UUID pid : gd.orderedPlayerIds) {
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                permanentIds.add(p.getId());
            }
        }

        List<String> problems = new ArrayList<>();
        for (UUID pid : gd.orderedPlayerIds) {
            for (Permanent p : gd.playerBattlefields.getOrDefault(pid, List.of())) {
                boolean isAura = p.getCard().getSubtypes().contains(CardSubtype.AURA);
                boolean isEquipment = p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT);
                UUID attachedTo = p.getAttachedTo();
                if (isAura) {
                    if (attachedTo == null) {
                        problems.add("aura " + p.getCard().getName() + " (" + p.getId()
                                + ") is on the battlefield attached to nothing");
                    } else if (!permanentIds.contains(attachedTo) && !gd.playerIds.contains(attachedTo)) {
                        problems.add("aura " + p.getCard().getName() + " (" + p.getId()
                                + ") is attached to nonexistent object " + attachedTo);
                    }
                } else if (isEquipment && attachedTo != null && !permanentIds.contains(attachedTo)) {
                    problems.add("equipment " + p.getCard().getName() + " (" + p.getId()
                            + ") is attached to nonexistent permanent " + attachedTo);
                }
            }
        }
        return problems.isEmpty() ? null : "attachment violation: " + String.join("; ", problems);
    }

    /**
     * CR 704.5a–c — with an empty stack and no pending input, a player at 0 or less life
     * (or with 10+ poison counters) must have lost: the game's status must be FINISHED.
     * Mirrors the guards in {@code GameOutcomeService.checkWinCondition} (can't-lose
     * effects, loss-type prevention, mid-resolution deferral) so states those legitimately
     * allow are not reported.
     */
    private String findGameOverViolation(GameData gd) {
        if (gd.status != GameStatus.RUNNING || gd.deferPlayerLossCheck) {
            return null;
        }
        for (UUID pid : gd.orderedPlayerIds) {
            int life = gd.getLife(pid);
            int poison = gd.playerPoisonCounters.getOrDefault(pid, 0);
            if (life > 0 && poison < 10) {
                continue;
            }
            if (!gameQueryService.canPlayerLoseGame(gd, pid)) {
                continue;
            }
            boolean loseFromLife = life <= 0 && gameQueryService.canPlayerLoseFromLife(gd, pid);
            boolean loseFromPoison = poison >= 10;
            if (!loseFromLife && !loseFromPoison) {
                continue;
            }
            return "game not finished: player " + pid + " is at " + life + " life / " + poison
                    + " poison with an empty stack, but the loss SBA has not ended the game";
        }
        return null;
    }
}
