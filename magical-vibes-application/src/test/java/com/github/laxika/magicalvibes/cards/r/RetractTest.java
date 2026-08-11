package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RetractTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifacts you control to their owners' hands")
    void returnsAllArtifactsYouControl() {
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new Retract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Icy Manipulator");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Icy Manipulator", "Angel's Feather");
    }

    @Test
    @DisplayName("Resolves when you control no artifacts")
    void resolvesWithNoArtifacts() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Retract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Retract");
    }
}
