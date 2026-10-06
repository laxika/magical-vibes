package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SacredFoundry.class)
class SacredFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Sacred Foundry enter untapped")
    void payingLifeEntersUntapped() {
        playSacredFoundry(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findSacredFoundry(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Sacred Foundry enter tapped")
    void decliningPaymentEntersTapped() {
        playSacredFoundry(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findSacredFoundry(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacred Foundry enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playSacredFoundry(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findSacredFoundry(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying exactly 2 life lets Sacred Foundry enter untapped")
    void payingExactLifeTotalEntersUntapped() {
        playSacredFoundry(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isZero();
        assertThat(findSacredFoundry(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacred Foundry produces red mana")
    void producesRedMana() {
        Permanent foundry = addSacredFoundryReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(foundry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacred Foundry produces white mana")
    void producesWhiteMana() {
        Permanent foundry = addSacredFoundryReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(foundry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacred Foundry can produce mana immediately after paying life on entry")
    void producesManaOnTurnItEnters() {
        playSacredFoundry(20);
        harness.handleMayAbilityChosen(player1, true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findSacredFoundry(player1).isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sacred Foundry offers its controller the life payment when put onto the battlefield")
    void enteringWithoutLandPlayOffersPaymentToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        Permanent foundry = harness.enterBattlefieldAndReturn(player2, new SacredFoundry());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
        assertThat(foundry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacred Foundry enters tapped when payment is declined after being put onto the battlefield")
    void enteringWithoutLandPlayCanDeclinePayment() {
        harness.setLife(player2, 10);
        Permanent foundry = harness.enterBattlefieldAndReturn(player2, new SacredFoundry());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(foundry.isTapped()).isTrue();
    }

    private void playSacredFoundry(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new SacredFoundry()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addSacredFoundryReady(Player player) {
        return addCreatureReady(player, new SacredFoundry());
    }

    private Permanent findSacredFoundry(Player player) {
        return findPermanent(player, "Sacred Foundry");
    }
}
