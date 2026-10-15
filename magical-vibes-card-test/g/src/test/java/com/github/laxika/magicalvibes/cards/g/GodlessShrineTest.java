package com.github.laxika.magicalvibes.cards.g;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GodlessShrine.class)
class GodlessShrineTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Godless Shrine enter untapped")
    void payingLifeEntersUntapped() {
        playShrine(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findShrine(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Godless Shrine enter tapped")
    void decliningPaymentEntersTapped() {
        playShrine(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findShrine(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Godless Shrine enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playShrine(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findShrine(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying exactly 2 life lets Godless Shrine enter untapped")
    void payingExactLifeTotalEntersUntapped() {
        playShrine(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isZero();
        assertThat(findShrine(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Godless Shrine produces white mana")
    void producesWhiteMana() {
        Permanent shrine = addShrineReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(shrine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Godless Shrine produces black mana")
    void producesBlackMana() {
        Permanent shrine = addShrineReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(shrine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Godless Shrine can produce mana immediately after paying life")
    void paidShrineCanProduceManaImmediately() {
        playShrine(20);
        harness.handleMayAbilityChosen(player1, true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(findShrine(player1).isTapped()).isTrue();
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Godless Shrine cannot produce mana after declining the payment")
    void declinedShrineCannotProduceMana() {
        playShrine(20);
        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(findShrine(player1).isTapped()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A player at exactly 2 life may decline Godless Shrine's payment")
    void exactLifeTotalMayDeclinePayment() {
        playShrine(2);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 2);
        assertThat(findShrine(player1).isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void playShrine(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new GodlessShrine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addShrineReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GodlessShrine());
    }

    private Permanent findShrine(Player player) {
        return findPermanent(player, "Godless Shrine");
    }
}
