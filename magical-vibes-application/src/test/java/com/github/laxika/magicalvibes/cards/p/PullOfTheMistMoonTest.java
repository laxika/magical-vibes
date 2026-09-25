package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PullOfTheMistMoon.class, GrizzlyBears.class, Island.class})
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
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualEnterExileHandCardChoice.class);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.perpetualEnterEffectsByCardId).containsKey(chosenCard.getId());

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
}
