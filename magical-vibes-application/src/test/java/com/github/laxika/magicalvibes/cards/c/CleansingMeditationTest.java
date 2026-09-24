package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.h.Hypochondria;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CleansingMeditation.class, Hypochondria.class, AvenTrooper.class})
class CleansingMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all enchantments without threshold")
    void destroysAllEnchantmentsWithoutThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Hypochondria());
        harness.addToBattlefield(player2, new Hypochondria());
        harness.addToBattlefield(player1, new AvenTrooper());
        harness.addToBattlefield(player2, new AvenTrooper());

        castCleansingMeditation();

        harness.assertNotOnBattlefield(player1, "Hypochondria");
        harness.assertNotOnBattlefield(player2, "Hypochondria");
        harness.assertOnBattlefield(player1, "Aven Trooper");
        harness.assertOnBattlefield(player2, "Aven Trooper");
    }

    @Test
    @DisplayName("Threshold returns only enchantments destroyed into your graveyard")
    void thresholdReturnsOnlyEnchantmentsDestroyedIntoYourGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Hypochondria());
        Card previousEnchantment = new Hypochondria();
        harness.setGraveyard(player1, List.of(
                previousEnchantment,
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper()));

        castCleansingMeditation();

        harness.assertOnBattlefield(player1, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(previousEnchantment)
                .hasSize(8);
    }

    @Test
    @DisplayName("Threshold is checked before the destruction adds cards to the graveyard")
    void thresholdIsCheckedBeforeDestruction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card enchantment = new Hypochondria();
        harness.addToBattlefield(player1, enchantment);
        harness.setGraveyard(player1, List.of(
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper()));

        castCleansingMeditation();

        harness.assertNotOnBattlefield(player1, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enchantment)
                .hasSize(8);
    }

    @Test
    @DisplayName("Threshold does not return enchantments put into an opponent's graveyard")
    void thresholdDoesNotReturnOpponentsEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Hypochondria());
        Card opponentEnchantment = new Hypochondria();
        harness.addToBattlefield(player2, opponentEnchantment);
        harness.setGraveyard(player1, List.of(
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper(), new AvenTrooper(), new AvenTrooper(),
                new AvenTrooper()));

        castCleansingMeditation();

        harness.assertOnBattlefield(player1, "Hypochondria");
        harness.assertNotOnBattlefield(player2, "Hypochondria");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentEnchantment);
    }

    private void castCleansingMeditation() {
        harness.setHand(player1, List.of(new CleansingMeditation()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
