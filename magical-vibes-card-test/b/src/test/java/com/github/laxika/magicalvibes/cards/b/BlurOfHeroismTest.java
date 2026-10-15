package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlurOfHeroism.class, BraveBrawler.class, GrizzlyBears.class})
class BlurOfHeroismTest extends BaseCardTest {

    @Test
    @DisplayName("Blur of Heroism draws a card when it enters")
    void drawsCardOnEnter() {
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new BlurOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player1, libraryCard.getName());
        harness.assertOnBattlefield(player1, "Blur of Heroism");
    }

    @Test
    @DisplayName("Blur of Heroism lets its controller cast Hero spells during combat")
    void grantsFlashToHeroSpells() {
        harness.addToBattlefield(player1, new BlurOfHeroism());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BraveBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Blur of Heroism does not grant flash to non-Hero spells")
    void doesNotGrantFlashToNonHeroSpells() {
        harness.addToBattlefield(player1, new BlurOfHeroism());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Its controller can cast a Hero during the opponent's turn")
    void grantsFlashDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new BlurOfHeroism());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BraveBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brave Brawler");
    }

    @Test
    @DisplayName("The opponent cannot use Blur of Heroism's flash permission")
    void doesNotGrantFlashToOpponent() {
        harness.addToBattlefield(player1, new BlurOfHeroism());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BraveBrawler()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The entry trigger draws exactly one card even after its source leaves")
    void drawTriggerSurvivesSourceLeaving() {
        BraveBrawler firstCard = new BraveBrawler();
        GrizzlyBears secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new BlurOfHeroism()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        harness.assertInHand(player1, firstCard.getName());
        harness.assertNotInHand(player1, secondCard.getName());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }
}
