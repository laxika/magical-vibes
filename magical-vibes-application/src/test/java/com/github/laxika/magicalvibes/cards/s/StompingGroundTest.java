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

@CardUsed(StompingGround.class)
class StompingGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Stomping Ground enter untapped")
    void payingLifeEntersUntapped() {
        playStompingGround(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findStompingGround(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Stomping Ground enter tapped")
    void decliningPaymentEntersTapped() {
        playStompingGround(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findStompingGround(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stomping Ground enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playStompingGround(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findStompingGround(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying exactly 2 life lets Stomping Ground enter untapped")
    void payingExactLifeTotalEntersUntapped() {
        playStompingGround(2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isZero();
        assertThat(findStompingGround(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Stomping Ground produces red mana")
    void producesRedMana() {
        Permanent stompingGround = addStompingGroundReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(stompingGround.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stomping Ground produces green mana")
    void producesGreenMana() {
        Permanent stompingGround = addStompingGroundReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(stompingGround.isTapped()).isTrue();
    }

    private void playStompingGround(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new StompingGround()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    @Test
    @DisplayName("Stomping Ground can produce mana immediately after paying life")
    void producesManaOnTurnItEnters() {
        playStompingGround(20);
        harness.handleMayAbilityChosen(player1, true);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(findStompingGround(player1).isTapped()).isTrue();
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The player playing Stomping Ground pays life, not their opponent")
    void secondPlayerPaysOwnLife() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 10);
        harness.setHand(player2, List.of(new StompingGround()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 8);
        assertThat(findStompingGround(player2).isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addStompingGroundReady(Player player) {
        Permanent stompingGround = harness.addToBattlefieldAndReturn(player, new StompingGround());
        stompingGround.setSummoningSick(false);
        return stompingGround;
    }

    private Permanent findStompingGround(Player player) {
        return findPermanent(player, "Stomping Ground");
    }
}
