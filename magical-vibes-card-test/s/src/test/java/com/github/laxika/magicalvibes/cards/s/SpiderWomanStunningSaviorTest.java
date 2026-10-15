package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CommonCrook;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PeterParkersCamera;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderWomanStunningSavior.class, PeterParkersCamera.class, Forest.class, CommonCrook.class, SoulSummons.class})
class SpiderWomanStunningSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creatures and artifacts enter tapped")
    void opponentsCreaturesAndArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CommonCrook(), "{1}{B}");
        harness.passBothPriorities();

        harness.castFromHand(player2, new PeterParkersCamera(), "{1}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Common Crook").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Peter Parker's Camera").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures and artifacts enter untapped")
    void controllersCreaturesAndArtifactsEnterUntapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());

        harness.castFromHand(player1, new CommonCrook(), "{1}{B}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new PeterParkersCamera(), "{1}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Common Crook").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Peter Parker's Camera").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's lands enter untapped")
    void opponentsLandsEnterUntapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's permanents enter tapped even when they are not cast")
    void noncastPermanentsEnterTapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());

        Permanent creature = harness.enterBattlefieldAndReturn(player2, new CommonCrook());
        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new PeterParkersCamera());

        assertThat(creature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spider-Woman does not tap permanents already on the battlefield")
    void existingPermanentsRemainUntapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CommonCrook());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PeterParkersCamera());

        Permanent spiderWoman = harness.enterBattlefieldAndReturn(player1, new SpiderWomanStunningSavior());

        assertThat(spiderWoman.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering tapped does not prevent untapping on the next untap step")
    void affectedPermanentsUntapNormally() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new CommonCrook());
        Permanent artifact = harness.enterBattlefieldAndReturn(player2, new PeterParkersCamera());
        assertThat(creature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's manifested land enters tapped as a face-down creature")
    void manifestedLandEntersTapped() {
        harness.addToBattlefield(player1, new SpiderWomanStunningSavior());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new SoulSummons(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isTapped()).isTrue();
    }
}
