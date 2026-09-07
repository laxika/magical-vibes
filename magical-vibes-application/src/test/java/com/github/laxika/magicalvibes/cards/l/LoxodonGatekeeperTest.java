package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonGatekeeper.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class LoxodonGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's artifacts, creatures, and lands enter tapped")
    void opponentsArtifactsCreaturesAndLandsEnterTapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player2, List.of(new Ornithopter(), new GrizzlyBears(), new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        gs.playCard(gd, player2, 0, 0, null, null);

        assertThat(findPermanent(player2, "Ornithopter").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's artifacts, creatures, and lands enter untapped")
    void controllersArtifactsCreaturesAndLandsEnterUntapped() {
        harness.addToBattlefield(player1, new LoxodonGatekeeper());
        harness.setHand(player1, List.of(new Ornithopter(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gs.playCard(gd, player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Ornithopter").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }
}
