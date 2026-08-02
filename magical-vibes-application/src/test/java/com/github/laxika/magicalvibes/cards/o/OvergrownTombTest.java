package com.github.laxika.magicalvibes.cards.o;

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

class OvergrownTombTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Overgrown Tomb enter untapped")
    void payingLifeEntersUntapped() {
        playOvergrownTomb(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findTomb(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Overgrown Tomb enter tapped")
    void decliningPaymentEntersTapped() {
        playOvergrownTomb(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findTomb(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Overgrown Tomb enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playOvergrownTomb(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findTomb(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Overgrown Tomb produces black mana")
    void producesBlackMana() {
        Permanent tomb = addTombReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(tomb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Overgrown Tomb produces green mana")
    void producesGreenMana() {
        Permanent tomb = addTombReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(tomb.isTapped()).isTrue();
    }

    private void playOvergrownTomb(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new OvergrownTomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addTombReady(Player player) {
        Permanent tomb = new Permanent(new OvergrownTomb());
        tomb.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tomb);
        return tomb;
    }

    private Permanent findTomb(Player player) {
        return findPermanent(player, "Overgrown Tomb");
    }
}
