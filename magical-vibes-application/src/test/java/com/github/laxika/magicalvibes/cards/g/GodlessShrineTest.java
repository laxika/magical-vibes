package com.github.laxika.magicalvibes.cards.g;

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

    private void playShrine(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new GodlessShrine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addShrineReady(Player player) {
        Permanent shrine = new Permanent(new GodlessShrine());
        shrine.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(shrine);
        return shrine;
    }

    private Permanent findShrine(Player player) {
        return findPermanent(player, "Godless Shrine");
    }
}
