package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderWomanStunningSavior.class, DarksteelRelic.class, Forest.class, GrizzlyBears.class})
class SpiderWomanStunningSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures and artifacts enter tapped")
    void opponentsCreaturesAndArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());
        harness.setHand(player2, List.of(new GrizzlyBears(), new DarksteelRelic()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Darksteel Relic").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures and artifacts enter untapped")
    void controllersCreaturesAndArtifactsEnterUntapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());
        harness.setHand(player1, List.of(new GrizzlyBears(), new DarksteelRelic()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Darksteel Relic").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands enter untapped")
    void opponentsLandsEnterUntapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, null, null);

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }
}
