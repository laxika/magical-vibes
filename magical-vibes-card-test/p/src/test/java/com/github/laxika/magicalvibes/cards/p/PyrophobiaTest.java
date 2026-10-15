package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImpostorOfTheSixthPride;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pyrophobia.class, AirElemental.class, GrizzlyBears.class, ImpostorOfTheSixthPride.class})
class PyrophobiaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the target creature")
    void dealsThreeDamageToTargetCreature() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castPyrophobia(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cowards can't block this turn, but other creatures can")
    void cowardsCantBlockThisTurn() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        Permanent coward = addCreatureReady(player2, new GrizzlyBears());
        coward.setTransientCreatureTypeOverride(CardSubtype.COWARD);
        Permanent nonCoward = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        castPyrophobia(target);
        attacker.setAttacking(true);

        assertThat(bls.canBlockAttacker(gd, coward, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, nonCoward, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void changelingEnteringAfterResolutionCantBlock() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        castPyrophobia(target);

        Permanent changeling = addCreatureReady(player2, new ImpostorOfTheSixthPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        assertThat(bls.canBlockAttacker(gd, changeling, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void restrictionTracksCreaturesBecomingAndCeasingToBeCowards() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castPyrophobia(target);
        attacker.setAttacking(true);

        blocker.setTransientCreatureTypeOverride(CardSubtype.COWARD);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        blocker.setTransientCreatureTypeOverride(CardSubtype.BEAR);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void illegalTargetPreventsCowardRestrictionFromTakingEffect() {
        Permanent target = addCreatureReady(player2, new AirElemental());
        Permanent coward = addCreatureReady(player2, new ImpostorOfTheSixthPride());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyrophobia()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();
        attacker.setAttacking(true);

        assertThat(bls.canBlockAttacker(gd, coward, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void lethalDamageStillPreventsCowardsControlledByEitherPlayerFromBlocking() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent coward = addCreatureReady(player1, new ImpostorOfTheSixthPride());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castPyrophobia(target);
        attacker.setAttacking(true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(bls.canBlockAttacker(gd, coward, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    private void castPyrophobia(Permanent target) {
        harness.setHand(player1, List.of(new Pyrophobia()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

}
