package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StructuralAssault.class, GrizzlyBears.class, HillGiant.class, HowlingMine.class,
        Ornithopter.class, Shatterstorm.class})
class StructuralAssaultTest extends BaseCardTest {

    @Test
    void destroysArtifactsAndDealsDamageEqualToArtifactsPutIntoGraveyardsThisTurn() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new StructuralAssault()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new Ornithopter());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new StructuralAssault()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }
}
