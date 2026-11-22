package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PullOfTheMistMoon.class, GrizzlyBears.class, Island.class, Memnite.class})
class PullOfTheMistMoonTest extends BaseCardTest {

    @Test
    void exilesTargetOpponentPermanentUntilSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PullOfTheMistMoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Pull of the Mist Moon");
        assertThat(gd.getCardsExiledByPermanent(source.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void kickerPerpetuallyGrantsChosenHandCardTheSameEtbAbility() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new PullOfTheMistMoon(), chosenCard, new Island()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(handCard(player1, 0)));
        assertThat(gd.perpetualAbilities(chosenCard.getId())).hasSize(1);

        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(
                findPermanent(player1, "Grizzly Bears").getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void returnsExiledPermanentWhenSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PullOfTheMistMoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent source = findPermanent(player1, "Pull of the Mist Moon");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
    }

    @Test
    void kickedHandChoiceStillResolvesWhenExileTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PullOfTheMistMoon(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCard(player1, 0)));
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotExileWhenSourceLeavesBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PullOfTheMistMoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Pull of the Mist Moon"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pull of the Mist Moon");
    }

    @Test
    void kickedWithOnlyLandInHandDoesNotPromptForHandCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PullOfTheMistMoon(), new Island()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The log doesn't name the card chosen from the hand")
    void logDoesNotNameTheChosenHandCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PullOfTheMistMoon(), new Memnite()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(handCard(player1, 0)));

        assertThat(gd.gameLog).noneMatch(line -> line.plainText().contains("Memnite"));
        assertThat(gd.gameLog).anyMatch(line -> line.plainText().contains("perpetually gains"));
    }

    /** The id of the card at {@code index} of {@code player}'s hand. */
    private UUID handCard(Player player, int index) {
        return gd.playerHands.get(player.getId()).get(index).getId();
    }
}
