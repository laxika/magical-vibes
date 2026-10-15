package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JudgeOfCurrents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilvergillAdept.class, JudgeOfCurrents.class, Island.class, CribSwap.class})
class SilvergillAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Merfolk in hand it cannot be cast for just {1}{U}")
    void requiresExtraThreeWithoutMerfolk() {
        // The Adept itself is a Merfolk but is on the stack, so it cannot satisfy its own reveal.
        harness.setHand(player1, List.of(new SilvergillAdept()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-Merfolk card in hand does not satisfy the reveal")
    void nonMerfolkCardDoesNotSatisfyReveal() {
        harness.setHand(player1, List.of(new SilvergillAdept(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Merfolk in an opponent's hand does not satisfy the reveal")
    void opponentMerfolkDoesNotSatisfyReveal() {
        harness.setHand(player1, List.of(new SilvergillAdept()));
        harness.setHand(player2, List.of(new JudgeOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {3} can be paid with mana when no Merfolk is revealed")
    void payTheThreeWithMana() {
        SilvergillAdept adept = new SilvergillAdept();
        harness.castFromHand(player1, adept, "{4}{U}"); // {1}{U} + {3}

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(adept.getId()));
    }

    @Test
    @DisplayName("Revealing a Merfolk card lets it be cast for {1}{U} and draws a card on enter")
    void revealMerfolkAndDrawOnEnter() {
        SilvergillAdept adept = new SilvergillAdept();
        JudgeOfCurrents merfolkInHand = new JudgeOfCurrents();
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(adept, merfolkInHand));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Hand after casting the Adept = just the revealed Merfolk (1 card); the enter trigger draws +1.
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the Adept
        harness.passBothPriorities(); // resolve the enter-the-battlefield draw trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(adept.getId()));
        // Revealing does not remove the Merfolk card from hand; the enter draw adds another card.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(merfolkInHand.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(drawnCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The Merfolk is publicly revealed while paying the casting cost")
    void revealsMerfolkBeforeSpellResolves() {
        harness.setHand(player1, List.of(new SilvergillAdept(), new JudgeOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals")
                && entry.plainText().contains("Judge of Currents"));
        harness.assertInHand(player1, "Judge of Currents");
        harness.assertNotOnBattlefield(player1, "Silvergill Adept");
    }

    @Test
    @DisplayName("A noncreature card with changeling can satisfy the Merfolk reveal cost")
    void kindredChangelingSatisfiesRevealCost() {
        harness.setHand(player1, List.of(new SilvergillAdept(), new CribSwap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silvergill Adept");
        harness.assertInHand(player1, "Crib Swap");
    }

    @Test
    @DisplayName("A Merfolk on the battlefield cannot satisfy the reveal from hand")
    void battlefieldMerfolkDoesNotSatisfyRevealCost() {
        harness.setHand(player1, List.of(new SilvergillAdept()));
        harness.addToBattlefield(player1, new JudgeOfCurrents());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revealing a Merfolk does not replace the required blue mana")
    void revealDoesNotWaiveBlueMana() {
        harness.setHand(player1, List.of(new SilvergillAdept(), new JudgeOfCurrents()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying the additional mana still triggers the card draw on entry")
    void payingAdditionalManaStillDraws() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.castFromHand(player1, new SilvergillAdept(), "{4}{U}");

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silvergill Adept");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entering without being cast draws for the creature's controller without a reveal cost")
    void enteringWithoutCastingDrawsForController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island()));

        harness.enterBattlefieldAndReturn(player2, new SilvergillAdept());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Silvergill Adept");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
