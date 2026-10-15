package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HardbristleBandit;
import com.github.laxika.magicalvibes.cards.s.SpinewoodsArmadillo;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutcasterTrailblazer.class, SpinewoodsArmadillo.class, HardbristleBandit.class})
class OutcasterTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("When Outcaster Trailblazer enters, its controller chooses a color and gets one mana")
    void enteringAddsOneManaOfChosenColor() {
        harness.castFromHand(player1, new OutcasterTrailblazer(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card when another creature with power 4 or greater enters under its controller's control")
    void drawsWhenAnotherHighPowerCreatureEnters() {
        harness.addToBattlefield(player1, new OutcasterTrailblazer());
        harness.castFromHand(player1, new SpinewoodsArmadillo(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when a creature with power less than 4 enters")
    void doesNotDrawForLowPowerCreature() {
        harness.addToBattlefield(player1, new OutcasterTrailblazer());
        harness.castFromHand(player1, new HardbristleBandit(), "{1}{G}");
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawForItsOwnEntry() {
        HardbristleBandit libraryCard = new HardbristleBandit();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new OutcasterTrailblazer(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void drawsForAnotherTrailblazerWithExactlyFourPower() {
        harness.addToBattlefield(player1, new OutcasterTrailblazer());
        HardbristleBandit libraryCard = new HardbristleBandit();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new OutcasterTrailblazer(), "{2}{G}");
        harness.passBothPriorities();
        for (int i = 0; i < 2 && gd.interaction.activeInteraction() == null; i++) {
            harness.passBothPriorities();
        }
        harness.handleListChoice(player1, "GREEN");
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void doesNotDrawForOpponentsHighPowerCreature() {
        harness.addToBattlefield(player1, new OutcasterTrailblazer());
        harness.setHand(player1, List.of());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SpinewoodsArmadillo(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void drawStillResolvesIfEnteringCreatureLosesPowerAfterTriggering() {
        harness.addToBattlefield(player1, new OutcasterTrailblazer());
        HardbristleBandit libraryCard = new HardbristleBandit();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new SpinewoodsArmadillo(), "{4}{G}{G}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SpinewoodsArmadillo)
                .findFirst().orElseThrow().setPowerModifier(-4);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void plotPaysCostAndAllowsFreeCastOnlyOnALaterTurn() {
        OutcasterTrailblazer trailblazer = new OutcasterTrailblazer();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(trailblazer));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Outcaster Trailblazer");
        assertThatThrownBy(() -> harness.castFromExile(player1, trailblazer.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.castFromExile(player1, trailblazer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.assertOnBattlefield(player1, "Outcaster Trailblazer");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
