package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({ArchelosLagoonMystic.class, Forest.class, GrizzlyBears.class, RootMaze.class})
class ArchelosLagoonMysticTest extends BaseCardTest {

    @Test
    void otherPermanentsEnterTappedWhileArchelosIsTapped() {
        Permanent archelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        archelos.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    void otherPermanentsEnterUntappedWhileArchelosIsUntapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.addToBattlefield(player1, new ArchelosLagoonMystic());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void tappedArchelosAffectsPermanentsEnteringUnderAnOpponentsControl() {
        Permanent archelos = harness.addToBattlefieldAndReturn(player1, new ArchelosLagoonMystic());
        archelos.tap();
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, null, null);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }
}
