package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyTendrilsRunDeep.class, Forest.class, GrizzlyBears.class})
class MyTendrilsRunDeepTest extends BaseCardTest {

    @Test
    void letsControllerPlayAnAdditionalLand() {
        harness.addToBattlefield(player1, new MyTendrilsRunDeep());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    void drawsTwoCardsAndAbandonsWhenControllerHasSixLands() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MyTendrilsRunDeep());
        addLands(player1, 6);
        GrizzlyBears firstDraw = new GrizzlyBears();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        resolveControllerEndStep();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme.getCard());
    }

    @Test
    void doesNotDrawOrAbandonWithFewerThanSixLands() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MyTendrilsRunDeep());
        addLands(player1, 5);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        resolveControllerEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(scheme.getCard());
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MyTendrilsRunDeep());
        addLands(player1, 6);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
    }

    @Test
    void rechecksLandCountWhenEndStepAbilityResolves() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MyTendrilsRunDeep());
        addLands(player1, 6);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeLast();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
    }

    @Test
    void opponentsLandsDoNotCountTowardThreshold() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new MyTendrilsRunDeep());
        addLands(player1, 5);
        addLands(player2, 6);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveControllerEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
