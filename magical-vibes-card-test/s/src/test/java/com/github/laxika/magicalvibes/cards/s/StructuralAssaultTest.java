package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StructuralAssault.class, GrizzlyBears.class, HillGiant.class, HowlingMine.class,
        Ornithopter.class, Shatterstorm.class, LiquimetalCoating.class})
class StructuralAssaultTest extends BaseCardTest {

    @Test
    void destroysArtifactsAndDealsDamageEqualToArtifactsPutIntoGraveyardsThisTurn() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new StructuralAssault()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    void countsArtifactsPutIntoGraveyardsEarlierInTheTurn() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.setHand(player1, List.of(new Shatterstorm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addToBattlefield(player2, new Ornithopter());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new StructuralAssault()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    @Test
    void dealsNoDamageWithoutArtifactsEnteringGraveyardsFromBattlefield() {
        harness.setGraveyard(player2, List.of(new HowlingMine()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StructuralAssault()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void countsPermanentsMadeIntoArtifactsBeforeTheyEnterGraveyards() {
        harness.addToBattlefield(player1, new LiquimetalCoating());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new StructuralAssault()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Liquimetal Coating");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
