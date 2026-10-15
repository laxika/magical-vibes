package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumedByGreed.class, GrizzlyBears.class, HillGiant.class, HolyDay.class})
class ConsumedByGreedTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices the opponent's creature with the greatest power without Gift")
    void sacrificesGreatestPowerCreatureWithoutGift() {
        Card smallerCreature = new GrizzlyBears();
        Card greatestCreature = new HillGiant();
        harness.addToBattlefield(player2, smallerCreature);
        harness.addToBattlefield(player2, greatestCreature);
        prepareSpell();

        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.castInstantWithGift(player1, 0, null, List.of(player2.getId()), false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Gift draws a card and returns a creature card after sacrificing the greatest power creature")
    void giftDrawsAndReturnsCreatureAfterSacrifice() {
        Card returnedCreature = new GrizzlyBears();
        Card smallerCreature = new GrizzlyBears();
        Card greatestCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.addToBattlefield(player2, smallerCreature);
        harness.addToBattlefield(player2, greatestCreature);
        prepareSpell();

        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.castInstantWithGift(player1, 0, null, List.of(player2.getId(), returnedCreature.getId()), true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(returnedCreature.getId()));
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Promised Gift requires a creature card target in the graveyard")
    void promisedGiftRequiresGraveyardTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(
                player1, 0, null, List.of(player2.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A promised Gift cannot target a noncreature card")
    void promisedGiftCannotTargetNoncreatureCard() {
        Card noncreatureCard = new HolyDay();
        harness.setGraveyard(player1, List.of(noncreatureCard));
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(
                player1, 0, null, List.of(player2.getId(), noncreatureCard.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ConsumedByGreed()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    void opponentChoosesOneOfTheCreaturesTiedForGreatestPower() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        var chosenId = gd.playerBattlefields.get(player2.getId()).get(2).getId();
        var otherId = gd.playerBattlefields.get(player2.getId()).get(1).getId();
        prepareSpell();

        harness.castInstantWithGift(player1, 0, null, List.of(player2.getId()), false);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosenId));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .anyMatch(permanent -> permanent.getId().equals(otherId))
                .noneMatch(permanent -> permanent.getId().equals(chosenId));
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void giftReturnsCreatureEvenWhenOpponentHasNoCreatures() {
        Card returnedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setLibrary(player2, List.of(new HolyDay()));
        prepareSpell();

        harness.castInstantWithGift(player1, 0, null,
                List.of(player2.getId(), returnedCreature.getId()), true);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Holy Day");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void missingGraveyardTargetDoesNotPreventGiftOrSacrifice() {
        Card returnedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setLibrary(player2, List.of(new HolyDay()));
        harness.addToBattlefield(player2, new HillGiant());
        prepareSpell();

        harness.castInstantWithGift(player1, 0, null,
                List.of(player2.getId(), returnedCreature.getId()), true);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Holy Day");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void promisedGiftCannotTargetOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, null,
                List.of(player2.getId(), creature.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetTheCasterForSacrifice() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, null,
                List.of(player1.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }
}
