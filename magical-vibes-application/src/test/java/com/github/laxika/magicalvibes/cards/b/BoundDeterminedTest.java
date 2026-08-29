package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RedElementalBlast;
import com.github.laxika.magicalvibes.cards.s.SimicSkySwallower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoundDetermined.class, SimicSkySwallower.class, Forest.class, Island.class,
        GrizzlyBears.class, CloudkinSeer.class, RedElementalBlast.class})
class BoundDeterminedTest extends BaseCardTest {

    @Test
    void boundReturnsUpToTheSacrificedCreaturesColorCountAndExilesItself() {
        Permanent swallower = harness.addToBattlefieldAndReturn(player1, new SimicSkySwallower());
        Card forest = new Forest();
        Card island = new Island();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, island, bears));

        BoundDetermined bound = new BoundDetermined();
        harness.setHand(player1, List.of(bound));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, swallower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), island.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bound.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Simic Sky Swallower");
    }

    @Test
    void determinedDrawsAndMakesLaterSpellsUncounterable() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new BoundDetermined()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);

        CloudkinSeer seer = new CloudkinSeer();
        harness.setHand(player1, List.of(seer));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new RedElementalBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, seer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudkin Seer");
    }
}
