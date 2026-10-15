package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BreedingPool.class)
class BreedingPoolTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Breeding Pool enter untapped")
    void payingLifeEntersUntapped() {
        playBreedingPool(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findPool(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Breeding Pool enter tapped")
    void decliningPaymentEntersTapped() {
        playBreedingPool(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findPool(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Breeding Pool enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playBreedingPool(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findPool(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying exactly 2 life lets Breeding Pool enter untapped")
    void payingExactLifeTotalEntersUntapped() {
        playBreedingPool(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isZero();
        assertThat(findPool(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Putting Breeding Pool onto the battlefield lets its controller pay life")
    void enteringWithoutLandPlayChargesController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        Permanent pool = harness.enterBattlefieldAndReturn(player2, new BreedingPool());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(pool.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining payment when Breeding Pool is put onto the battlefield makes it enter tapped")
    void enteringWithoutLandPlayCanDeclinePayment() {
        harness.setLife(player2, 10);

        Permanent pool = harness.enterBattlefieldAndReturn(player2, new BreedingPool());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(pool.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Breeding Pool produces green mana")
    void producesGreenMana() {
        Permanent pool = addPoolReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Breeding Pool produces blue mana")
    void producesBlueMana() {
        Permanent pool = addPoolReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.isTapped()).isTrue();
    }

    private void playBreedingPool(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new BreedingPool()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addPoolReady(Player player) {
        Permanent pool = harness.addToBattlefieldAndReturn(player, new BreedingPool());
        pool.setSummoningSick(false);
        return pool;
    }

    private Permanent findPool(Player player) {
        return findPermanent(player, "Breeding Pool");
    }
}
