package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player chooses any number of creatures they control. During that player's next turn, the
 * chosen creatures attack if able, and other creatures can't attack. At the beginning of that
 * turn's end step, destroy each of the chosen creatures that didn't attack this turn."
 * (Oracle en-Vec.)
 *
 * <p>Targets a player — pair on a card with a {@code PlayerPredicateTargetFilter} narrowing it to
 * an opponent. At resolution the targeted player picks any number of their creatures through a
 * {@code MultiPermanentChoiceContext.ChooseCreaturesToAttackNextTurn}; the chosen set (possibly
 * empty) is recorded in {@code GameData.chosenAttackersNextTurn}, keyed by that player.
 *
 * <p>When that player's turn begins {@code TurnProgressionService} promotes the set to
 * {@code GameData.chosenAttackersThisTurn} and queues one
 * {@code DestroyPermanentIfDidNotAttackAtEndStep} per chosen creature. During that turn
 * {@code AttackLegalityService} bars every creature outside the set from attacking and counts an
 * "attacks if able" requirement for every creature inside it — so an empty choice locks combat
 * down entirely.
 */
public record TargetPlayerChoosesCreaturesToAttackNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
