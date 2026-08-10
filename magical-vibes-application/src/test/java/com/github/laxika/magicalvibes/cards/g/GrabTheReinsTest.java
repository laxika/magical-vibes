package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrabTheReinsTest extends BaseCardTest {

    @Test
    @DisplayName("Control mode gains control and grants haste until end of turn")
    void controlModeGainsControlAndHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        cast(new int[]{0}, List.of(target.getId()), false);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Damage mode sacrifices a creature and deals its effective power to the target")
    void damageModeSacrificesAndDealsPowerDamage() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        cast(new int[]{1}, player2.getId(), List.of(), false);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Entwine pays {2}{R} and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent stolen = addCreatureReady(player2, new GrizzlyBears());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        cast(new int[]{0, 1}, player2.getId(), List.of(stolen.getId()), true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Control mode cannot target a noncreature")
    void controlModeRequiresCreatureTarget() {
        harness.setHand(player1, List.of(new GrabTheReins()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        cast(modes, null, targetIds, entwined);
    }

    private void cast(int[] modes, java.util.UUID targetId, List<java.util.UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new GrabTheReins()));
        harness.addMana(player1, ManaColor.RED, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 5 : 3);
        harness.getGameService().playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, modes), targetId, null, targetIds, List.of());
        harness.passBothPriorities();
    }
}
