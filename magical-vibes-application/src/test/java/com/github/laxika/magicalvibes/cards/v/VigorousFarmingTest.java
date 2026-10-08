package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RootMaze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VigorousFarming.class, Forest.class, FutureSight.class, GrizzlyBears.class, RootMaze.class})
class VigorousFarmingTest extends BaseCardTest {

    @Test
    void controlledLandsEnterUntapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.addToBattlefield(player1, new VigorousFarming());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "Untapped");

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void opponentsLandsStillEnterTapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.addToBattlefield(player1, new VigorousFarming());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }

    @Test
    void multipleGrantsStackAndOnlyEnhanceTheSelectedLand() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new VigorousFarming());
        harness.addToBattlefield(player1, new VigorousFarming());
        Permanent ordinaryForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromLibraryTop(player1);
        Permanent enhancedForest = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ordinaryForest));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(enhancedForest));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    void opponentsEndStepDoesNotEnhanceYourLibraryLand() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new VigorousFarming());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromLibraryTop(player1);
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).size() - 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void topmostLandCardPerpetuallyAddsGreenWhenTapped() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new VigorousFarming());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);
        harness.castFromLibraryTop(player1);

        Permanent forest = findPermanent(player1, "Forest");
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
