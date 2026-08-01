package com.github.laxika.magicalvibes.cards.h;

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

class HallowedFountainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Hallowed Fountain enter untapped")
    void payingLifeEntersUntapped() {
        playHallowedFountain(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findFountain(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Hallowed Fountain enter tapped")
    void decliningPaymentEntersTapped() {
        playHallowedFountain(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findFountain(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hallowed Fountain enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playHallowedFountain(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findFountain(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hallowed Fountain produces white mana")
    void producesWhiteMana() {
        Permanent fountain = addFountainReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hallowed Fountain produces blue mana")
    void producesBlueMana() {
        Permanent fountain = addFountainReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(fountain.isTapped()).isTrue();
    }

    private void playHallowedFountain(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new HallowedFountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addFountainReady(Player player) {
        Permanent fountain = new Permanent(new HallowedFountain());
        fountain.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(fountain);
        return fountain;
    }

    private Permanent findFountain(Player player) {
        return findPermanent(player, "Hallowed Fountain");
    }
}
