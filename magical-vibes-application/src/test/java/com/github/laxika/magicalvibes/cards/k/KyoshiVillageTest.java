package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KyoshiVillage.class)
class KyoshiVillageTest extends BaseCardTest {

    @Test
    @DisplayName("Kyoshi Village enters the battlefield tapped")
    void entersBattlefieldTapped() {
        playVillage();

        assertThat(findPermanent(player1, "Kyoshi Village").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Kyoshi Village adds green mana")
    void addsGreenMana() {
        Permanent village = addReadyVillage(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(village.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Kyoshi Village adds white mana")
    void addsWhiteMana() {
        Permanent village = addReadyVillage(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(village.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Kyoshi Village draws a card")
    void sacrificingDrawsCard() {
        addReadyVillage(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Kyoshi Village");
        harness.assertInGraveyard(player1, "Kyoshi Village");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    private void playVillage() {
        harness.setHand(player1, List.of(new KyoshiVillage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
    }

    private Permanent addReadyVillage(Player player) {
        Permanent village = new Permanent(new KyoshiVillage());
        village.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(village);
        return village;
    }
}
