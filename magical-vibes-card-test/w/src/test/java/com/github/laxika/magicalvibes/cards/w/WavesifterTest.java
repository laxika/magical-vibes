package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MarneusCalgar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wavesifter.class, MarneusCalgar.class})
class WavesifterTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast Wavesifter creates two Clues and remains on the battlefield")
    void hardcastCreatesTwoClues() {
        harness.setHand(player1, List.of(new Wavesifter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        harness.assertOnBattlefield(player1, "Wavesifter");
    }

    @Test
    @DisplayName("Evoke Wavesifter creates two Clues and sacrifices it")
    void evokeCreatesTwoCluesAndSacrificesSelf() {
        harness.setHand(player1, List.of(new Wavesifter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Wavesifter - sacrifice this creature");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Wavesifter");
        harness.assertInGraveyard(player1, "Wavesifter");
    }

    @Test
    @DisplayName("Investigation resolves even after the evoke sacrifice")
    void investigatesAfterSacrifice() {
        harness.setHand(player1, List.of(new Wavesifter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2: Wavesifter's ETB ability");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wavesifter");
        harness.assertNotOnBattlefield(player1, "Wavesifter");
        assertThat(findPermanents(player1, "Clue")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each investigation creates a separate token event for Marneus Calgar")
    void investigationsTriggerTokenEntrySeparately() {
        harness.addToBattlefield(player1, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new Wavesifter(), new Wavesifter(), new Wavesifter()));

        harness.enterBattlefieldAndReturn(player1, new Wavesifter());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Wavesifter");
    }

    @Test
    @DisplayName("A Clue costs two mana and is sacrificed to draw one card")
    void clueCanBeSacrificedToDraw() {
        harness.setLibrary(player1, List.of(new Wavesifter(), new Wavesifter()));
        harness.enterBattlefieldAndReturn(player1, new Wavesifter());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Clue"));

        harness.activateAbility(player1, clueIndex, null, null);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Wavesifter");
    }
}
