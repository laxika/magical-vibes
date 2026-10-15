package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GretaSweettoothScourge.class, GrizzlyBears.class})
class GretaSweettoothScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token")
    void entersWithFoodToken() {
        castGreta();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Sacrificing a Food puts a +1/+1 counter on target creature")
    void sacrificesFoodForCounterOnTargetCreature() {
        Permanent greta = castGreta();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, greta), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Sacrificing a Food draws a card and loses 1 life")
    void sacrificesFoodToDrawAndLoseLife() {
        Permanent greta = castGreta();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, greta), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("The counter ability can only be activated at sorcery speed")
    void counterAbilityRequiresSorcerySpeed() {
        Permanent greta = castGreta();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, greta), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("The counter ability cannot target a noncreature permanent")
    void counterAbilityRequiresCreatureTarget() {
        Permanent greta = castGreta();
        Permanent food = findPermanent(player1, "Food");

        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, greta), 0, null, food.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    void foodTokenCanBeSacrificedToGainThreeLife() {
        castGreta();
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, food), 0, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        resolveAllTriggers();
        harness.assertLife(player1, 23);
    }

    @Test
    void tappedFoodCanPayForCounterOnGreta() {
        Permanent greta = castGreta();
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, greta), 0, null, greta.getId());

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(greta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(greta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        harness.assertLife(player1, 20);
    }

    @Test
    void drawAbilityCanBeActivatedDuringOpponentsUpkeep() {
        Permanent greta = castGreta();
        harness.setLibrary(player1, List.of(new GretaSweettoothScourge()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, greta), 1, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        resolveAllTriggers();
        harness.assertInHand(player1, "Greta, Sweettooth Scourge");
        harness.assertLife(player1, 19);
    }

    @Test
    void cannotActivateEitherAbilityWithoutFood() {
        Permanent greta = addCreatureReady(player1, new GretaSweettoothScourge());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, greta), 0, null, greta.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, greta), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThat(greta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }
    private Permanent castGreta() {
        harness.castFromHand(player1, new GretaSweettoothScourge(), "{1}{B}{G}");
        resolveAllTriggers();
        return findPermanent(player1, "Greta, Sweettooth Scourge");
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
