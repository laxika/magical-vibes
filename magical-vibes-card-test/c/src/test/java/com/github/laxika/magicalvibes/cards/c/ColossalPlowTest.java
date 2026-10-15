package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.cards.r.ReplicatingRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossalPlow.class, GrizzlyBears.class, HillGiant.class, SerraAngel.class,
        RavenousLindwurm.class, ReplicatingRing.class})
class ColossalPlowTest extends BaseCardTest {

    @Test
    void crewAnimatesPlowAndTapsCreaturesWithTotalPowerSix() {
        Permanent plow = addPlowReady(player1);
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(plow.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, plow)).isTrue();
        assertThat(angel.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    void attackingAddsThreePersistentWhiteManaAndGainsThreeLife() {
        harness.setLife(player1, 10);
        addPlowReady(player1);
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(3);
        assertThat(pool.getPersistentMana(ManaColor.WHITE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(3);
    }

    @Test
    void cannotCrewWithoutSixTotalCreaturePower() {
        addPlowReady(player1);
        addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void summoningSickCreatureCanCrewWithoutAwardingAttackBenefits() {
        Permanent plow = addPlowReady(player1);
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new RavenousLindwurm());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wurm.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, plow)).isTrue();
        harness.assertLife(player1, 10);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void attackManaAndCrewAnimationExpireAtTurnEnd() {
        Permanent plow = addPlowReady(player1);
        addCreatureReady(player1, new RavenousLindwurm());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, plow)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void ordinaryManaAddedAfterSpendingAllAttackManaDoesNotPersist() {
        addPlowReady(player1);
        addCreatureReady(player1, new RavenousLindwurm());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new ReplicatingRing()));
        harness.castArtifact(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    private Permanent addPlowReady(Player player) {
        return addCreatureReady(player, new ColossalPlow());
    }
}
