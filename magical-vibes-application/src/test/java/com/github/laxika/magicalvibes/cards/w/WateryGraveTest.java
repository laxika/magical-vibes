package com.github.laxika.magicalvibes.cards.w;

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

class WateryGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life lets Watery Grave enter untapped")
    void payingLifeEntersUntapped() {
        playWateryGrave(20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(findGrave(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the life payment makes Watery Grave enter tapped")
    void decliningPaymentEntersTapped() {
        playWateryGrave(20);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findGrave(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Watery Grave enters tapped when its controller cannot pay 2 life")
    void insufficientLifeEntersTappedWithoutPrompt() {
        playWateryGrave(1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(findGrave(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Watery Grave produces blue mana")
    void producesBlueMana() {
        Permanent grave = addGraveReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(grave.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Watery Grave produces black mana")
    void producesBlackMana() {
        Permanent grave = addGraveReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(grave.isTapped()).isTrue();
    }

    private void playWateryGrave(int life) {
        harness.setLife(player1, life);
        harness.setHand(player1, List.of(new WateryGrave()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addGraveReady(Player player) {
        Permanent grave = new Permanent(new WateryGrave());
        grave.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(grave);
        return grave;
    }

    private Permanent findGrave(Player player) {
        return findPermanent(player, "Watery Grave");
    }
}
