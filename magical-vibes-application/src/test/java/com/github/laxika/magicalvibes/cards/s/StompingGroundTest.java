package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addStompingGroundReady(Player player) {
        Permanent stompingGround = new Permanent(new StompingGround());
        stompingGround.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(stompingGround);
        return stompingGround;
    }

    private Permanent findStompingGround(Player player) {
        return findPermanent(player, "Stomping Ground");
    }
}
