package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatTrainHeist.class, GrizzlyBears.class})
class GreatTrainHeistTest extends BaseCardTest {

    @Test
    @DisplayName("The untap mode untaps your creatures and adds a combat during your combat")
    void untapModeAddsCombatDuringYourCombat() {
        Permanent ownCreature = addTappedCreature(player1);
        Permanent opponentCreature = addTappedCreature(player2);

        cast(new int[]{0}, List.of(), 4, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
    }

    @Test
    @DisplayName("The untap mode does not add a combat outside your combat")
    void untapModeDoesNotAddCombatOutsideCombat() {
        Permanent ownCreature = addTappedCreature(player1);

        cast(new int[]{0}, List.of(), 4, TurnStep.PRECOMBAT_MAIN);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("The boost mode gives your creatures +1/+0 and first strike until end of turn")
    void boostMode() {
        Permanent ownCreature = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);

        cast(new int[]{1}, List.of(), 3, TurnStep.PRECOMBAT_MAIN);

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The Treasure mode creates one tapped Treasure for each creature dealing combat damage to the chosen opponent")
    void treasureMode() {
        addCreature(player1);
        addCreature(player1);

        cast(new int[]{2}, List.of(player2.getId()), 2, TurnStep.PRECOMBAT_MAIN);

        declareAttackers(player1, List.of(0, 1));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("The Treasure mode requires an opponent target")
    void treasureModeRejectsControllerAsTarget() {
        harness.setHand(player1, List.of(new GreatTrainHeist()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{2}, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addTappedCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addCreature(player);
        creature.tap();
        return creature;
    }

    private void cast(int[] modes, List<UUID> targets, int totalMana, TurnStep step) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.setHand(player1, List.of(new GreatTrainHeist()));
        harness.addMana(player1, ManaColor.RED, totalMana);
        harness.castModalInstantWithModes(player1, 0, 1, 3, modes, targets);
        harness.passBothPriorities();
    }
}
