package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Delete.class, AlphaMyr.class, GrizzlyBears.class})
class DeleteTest extends BaseCardTest {

    @Test
    void dealsXDamageToEachNonartifactCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Delete()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotDamageArtifactCreatures() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Delete()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(artifactCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Alpha Myr");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void marksNonlethalDamageAndSparesArtifactCreaturesOnBothSides() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Delete()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownArtifact.getMarkedDamage()).isZero();
        assertThat(opposingArtifact.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Alpha Myr");
        harness.assertOnBattlefield(player2, "Alpha Myr");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void zeroXDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Delete()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
