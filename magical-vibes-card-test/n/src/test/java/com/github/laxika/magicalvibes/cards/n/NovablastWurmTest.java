package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BojukaBog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NovablastWurm.class, GrizzlyBears.class, BojukaBog.class})
class NovablastWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking destroys all other creatures on both battlefields")
    void attackingDestroysAllOtherCreatures() {
        Permanent wurm = addCreatureReady(player1, new NovablastWurm());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Novablast Wurm itself survives its attack trigger")
    void sourceSurvivesItsAttackTrigger() {
        Permanent wurm = addCreatureReady(player1, new NovablastWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Only the attacking Wurm is spared, not other Wurms with the same name")
    void destroysOtherWurmsWithTheSameName() {
        Permanent attacker = addCreatureReady(player1, new NovablastWurm());
        Permanent ownWurm = addCreatureReady(player1, new NovablastWurm());
        Permanent opposingWurm = addCreatureReady(player2, new NovablastWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker).doesNotContain(ownWurm);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingWurm);
        harness.assertInGraveyard(player1, "Novablast Wurm");
        harness.assertInGraveyard(player2, "Novablast Wurm");
    }

    @Test
    @DisplayName("Two attacking Wurms destroy each other even after one trigger's source dies")
    void twoAttackingWurmsDestroyEachOther() {
        Permanent first = addCreatureReady(player1, new NovablastWurm());
        Permanent second = addCreatureReady(player1, new NovablastWurm());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("The attack trigger leaves noncreature permanents on both battlefields intact")
    void noncreaturePermanentsSurvive() {
        Permanent wurm = addCreatureReady(player1, new NovablastWurm());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new BojukaBog());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new BojukaBog());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm, ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingLand);
    }
}
