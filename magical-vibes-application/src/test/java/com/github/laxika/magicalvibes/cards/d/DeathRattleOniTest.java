package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathRattleOni.class, GiantSpider.class, GrizzlyBears.class, Shock.class})
class DeathRattleOniTest extends BaseCardTest {

    @Test
    @DisplayName("Costs two less for each creature that died this turn")
    void costReductionCountsCreatureDeaths() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new DeathRattleOni()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Destroys other damaged creatures when it enters")
    void destroysOtherDamagedCreatures() {
        Permanent damaged = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new DeathRattleOni()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castInstant(player1, 0, damaged.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent oni = findPermanent(player1, "Death-Rattle Oni");
        gd.permanentsDealtDamageThisTurn.add(oni.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Death-Rattle Oni");
    }

    @Test
    void reductionCountsBothPlayersDeathsAndLeavesColoredCost() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent another = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new DeathRattleOni()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        for (Permanent creature : List.of(own, opposing, another, fourth)) {
            harness.castInstant(player1, 0, creature.getId());
            harness.passBothPriorities();
        }
        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Death-Rattle Oni");
    }

    @Test
    void triggerChecksDamageAtResolutionAndSparesItsDamagedSource() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new DeathRattleOni(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent oni = findPermanent(player1, "Death-Rattle Oni");
        harness.addMana(player1, ManaColor.RED, 3);
        for (Permanent creature : List.of(own, opposing, oni)) {
            harness.castInstant(player1, 0, creature.getId());
            harness.passBothPriorities();
        }
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player1, "Death-Rattle Oni");
    }

    @Test
    void canBeCastOnOpponentsTurnWithNoDeaths() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new DeathRattleOni()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Death-Rattle Oni");
    }
}
