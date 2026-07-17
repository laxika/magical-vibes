package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles {@link SacrificePermanentsSequenceCost} — sacrifice one distinct permanent per filter, in
 * order (Angel's Herald: "Sacrifice a green creature, a white creature, and a blue creature").
 *
 * <p>Payment is resolved one choice at a time; {@code chosenSoFar} (seeded from the pending-choice
 * context across async prompts, and grown in place during a synchronous auto-pay loop) tells the
 * handler which slot is being paid — the slot index is simply {@code chosenSoFar.size()}. A slot is
 * only offered permanents whose selection still leaves a complete system of distinct representatives
 * for the remaining slots, so no pick can strand a later slot after the cost is partially paid.</p>
 */
public class SequencePermanentSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final SacrificePermanentsSequenceCost cost;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentSacrificeAction sacrificeAction;
    private final List<UUID> chosenSoFar;

    public SequencePermanentSacrificeCostHandler(SacrificePermanentsSequenceCost cost,
                                                 PredicateEvaluationService predicateEvaluationService,
                                                 PermanentSacrificeAction sacrificeAction,
                                                 List<UUID> chosenSoFar) {
        this.cost = cost;
        this.predicateEvaluationService = predicateEvaluationService;
        this.sacrificeAction = sacrificeAction;
        this.chosenSoFar = new ArrayList<>(chosenSoFar == null ? List.of() : chosenSoFar);
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return cost.filters().size(); }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (!remainingSlotsMatchable(gameData, playerId)) {
            throw new IllegalStateException("Not enough permanents to sacrifice for: "
                    + String.join(", ", cost.descriptions()));
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        int slot = chosenSoFar.size();
        if (slot >= cost.filters().size()) return List.of();
        List<Permanent> available = availablePermanents(gameData, playerId);
        PermanentPredicate currentFilter = cost.filters().get(slot);
        List<PermanentPredicate> laterSlots = cost.filters().subList(slot + 1, cost.filters().size());
        return available.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, currentFilter))
                // Picking p must not strand a later slot: the rest must still match without p.
                .filter(p -> canMatch(gameData, laterSlots, withoutPermanent(available, p)))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        int slot = chosenSoFar.size();
        if (slot >= cost.filters().size()) {
            throw new IllegalStateException("No sacrifice slots remaining");
        }
        if (!getValidChoiceIds(gameData, player.getId()).contains(chosen.getId())) {
            throw new IllegalStateException("Must sacrifice a permanent matching: " + cost.descriptions().get(slot));
        }
        sacrificeAction.sacrifice(gameData, player, chosen);
        chosenSoFar.add(chosen.getId());
    }

    @Override
    public String getPromptMessage(int remaining) {
        int slot = chosenSoFar.size();
        String label = slot < cost.descriptions().size() ? cost.descriptions().get(slot) : "permanent";
        return "Choose a permanent to sacrifice (" + label + ").";
    }

    @Override
    public boolean canPayRemaining(GameData gameData, UUID playerId, int remaining) {
        return remainingSlotsMatchable(gameData, playerId);
    }

    @Override
    public boolean shouldAutoPayAll(GameData gameData, UUID playerId, int remaining) {
        // Only the final pick may auto-pay: the auto-pay path pays every current valid choice at
        // once and then treats the whole cost as done, which is only correct when a single slot
        // with a single forced choice is left.
        return remaining <= 1 && getValidChoiceIds(gameData, playerId).size() <= 1;
    }

    /** Battlefield permanents the payer controls that are still available (not already sacrificed). */
    private List<Permanent> availablePermanents(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> !chosenSoFar.contains(p.getId()))
                .toList();
    }

    private static List<Permanent> withoutPermanent(List<Permanent> permanents, Permanent excluded) {
        return permanents.stream().filter(p -> p != excluded).toList();
    }

    /** True if the remaining (unpaid) slots can each be assigned a distinct available permanent. */
    private boolean remainingSlotsMatchable(GameData gameData, UUID playerId) {
        int slot = chosenSoFar.size();
        List<PermanentPredicate> remainingSlots = cost.filters().subList(slot, cost.filters().size());
        return canMatch(gameData, remainingSlots, availablePermanents(gameData, playerId));
    }

    /**
     * True if every slot predicate can be matched to a distinct permanent (a system of distinct
     * representatives). Kuhn's bipartite matching over slots (small) against candidate permanents.
     */
    private boolean canMatch(GameData gameData, List<PermanentPredicate> slots, List<Permanent> permanents) {
        int[] slotToPermanent = new int[slots.size()];
        java.util.Arrays.fill(slotToPermanent, -1);
        int[] permanentToSlot = new int[permanents.size()];
        java.util.Arrays.fill(permanentToSlot, -1);
        for (int s = 0; s < slots.size(); s++) {
            boolean[] visited = new boolean[permanents.size()];
            if (!augment(gameData, s, slots, permanents, visited, permanentToSlot)) {
                return false;
            }
        }
        return true;
    }

    private boolean augment(GameData gameData, int slot, List<PermanentPredicate> slots,
                            List<Permanent> permanents, boolean[] visited, int[] permanentToSlot) {
        PermanentPredicate filter = slots.get(slot);
        for (int p = 0; p < permanents.size(); p++) {
            if (visited[p]) continue;
            if (!predicateEvaluationService.matchesPermanentPredicate(gameData, permanents.get(p), filter)) continue;
            visited[p] = true;
            if (permanentToSlot[p] == -1
                    || augment(gameData, permanentToSlot[p], slots, permanents, visited, permanentToSlot)) {
                permanentToSlot[p] = slot;
                return true;
            }
        }
        return false;
    }
}
