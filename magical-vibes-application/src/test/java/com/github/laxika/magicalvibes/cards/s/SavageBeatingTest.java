package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SavageBeatingTest extends BaseCardTest {

    @Test
    @DisplayName("The double-strike mode affects only your creatures until end of turn")
    void doubleStrikeMode() {
        Permanent ownCreature = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);

        cast(new int[]{0}, 2, 3);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The extra-combat mode untaps your creatures and queues another combat")
    void extraCombatMode() {
        Permanent ownCreature = addTappedCreature(player1);
        Permanent opponentCreature = addTappedCreature(player2);

        cast(new int[]{1}, 2, 3);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.additionalCombatPhasesOnly).isZero();
    }

    @Test
    @DisplayName("Entwine pays {1}{R} and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent ownCreature = addTappedCreature(player1);
        Permanent opponentCreature = addTappedCreature(player2);

        cast(new int[]{0, 1}, 3, 4);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("It can be cast only during your combat")
    void castTimingRestriction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        prepareManaAndHand(2, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        prepareManaAndHand(2, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        prepareManaAndHand(2, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = addCreature(player);
        creature.tap();
        return creature;
    }

    private void cast(int[] modes, int redMana, int colorlessMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        prepareManaAndHand(redMana, colorlessMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of());
        harness.passBothPriorities();
    }

    private void prepareManaAndHand(int redMana, int colorlessMana) {
        harness.setHand(player1, List.of(new SavageBeating()));
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
    }
}
