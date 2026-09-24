package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LumengridWarden;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GrabTheReins.class, LumengridWarden.class})
class GrabTheReinsTest extends BaseCardTest {

    @Test
    @DisplayName("Control mode gains control and grants haste until end of turn")
    void controlModeGainsControlAndHaste() {
        Permanent target = addCreatureReady(player2, new LumengridWarden());
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
        Permanent sacrifice = addCreatureReady(player1, new LumengridWarden());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        cast(new int[]{1}, List.of(player2.getId()), false);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Lumengrid Warden");
    }

    @Test
    @DisplayName("Entwine pays {2}{R} and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent stolen = addCreatureReady(player2, new LumengridWarden());
        Permanent sacrifice = addCreatureReady(player1, new LumengridWarden());
        harness.setLife(player2, 20);

        cast(new int[]{0, 1}, List.of(stolen.getId(), player2.getId()), true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(stolen.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Lumengrid Warden");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Damage mode requires an any target when cast")
    void damageModeRequiresTargetAtCastTime() {
        addCreatureReady(player1, new LumengridWarden());
        prepareCast(false);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage mode does not resolve after its target leaves the battlefield")
    void damageModeDoesNotResolveAfterTargetLeavesBattlefield() {
        Permanent sacrifice = addCreatureReady(player1, new LumengridWarden());
        Permanent target = addCreatureReady(player2, new LumengridWarden());
        prepareCast(false);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
    }

    @Test
    @DisplayName("Control mode cannot target a noncreature")
    void controlModeRequiresCreatureTarget() {
        prepareCast(false);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds, boolean entwined) {
        prepareCast(entwined);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast(boolean entwined) {
        harness.setHand(player1, List.of(new GrabTheReins()));
        harness.addMana(player1, ManaColor.RED, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 5 : 3);
    }
}
