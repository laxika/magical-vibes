package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CleansingMeditation.class, GloriousAnthem.class, GrizzlyBears.class})
class CleansingMeditationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all enchantments without threshold")
    void destroysAllEnchantmentsWithoutThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());

        castCleansingMeditation();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Threshold returns only enchantments destroyed into your graveyard")
    void thresholdReturnsOnlyEnchantmentsDestroyedIntoYourGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GloriousAnthem());
        Card previousEnchantment = new GloriousAnthem();
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                previousEnchantment,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        castCleansingMeditation();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(previousEnchantment)
                .hasSize(8);
    }

    @Test
    @DisplayName("Threshold is checked before the destruction adds cards to the graveyard")
    void thresholdIsCheckedBeforeDestruction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card enchantment = new GloriousAnthem();
        harness.addToBattlefield(player1, enchantment);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        castCleansingMeditation();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enchantment)
                .hasSize(8);
    }

    @Test
    @DisplayName("Threshold does not return enchantments put into an opponent's graveyard")
    void thresholdDoesNotReturnOpponentsEnchantments() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GloriousAnthem());
        Card opponentEnchantment = new GloriousAnthem();
        harness.addToBattlefield(player2, opponentEnchantment);
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));

        castCleansingMeditation();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentEnchantment);
    }

    private void castCleansingMeditation() {
        harness.setHand(player1, new ArrayList<>(List.of(new CleansingMeditation())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
