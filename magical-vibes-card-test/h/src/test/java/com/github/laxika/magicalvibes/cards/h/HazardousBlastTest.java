package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.cards.s.SawbladeScamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazardousBlast.class, ContagiousVorrac.class, SawbladeScamp.class})
class HazardousBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage only to creatures opponents control")
    void damagesOnlyOpponentsCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());

        castHazardousBlast();

        assertThat(own.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents' creatures can't block this turn")
    void preventsOpponentsCreaturesFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new ContagiousVorrac());
        addCreatureReady(player2, new ContagiousVorrac());

        castHazardousBlast();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures entering after resolution take no damage but still can't block")
    void preventsLaterCreaturesFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new ContagiousVorrac());

        castHazardousBlast();

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        assertThat(blocker.getMarkedDamage()).isZero();

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kills one-toughness creatures and damages every surviving opposing creature")
    void damagesEveryOpposingCreatureAndKillsLethallyDamagedCreatures() {
        harness.addToBattlefield(player2, new SawbladeScamp());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());

        castHazardousBlast();

        harness.assertNotOnBattlefield(player2, "Sawblade Scamp");
        harness.assertInGraveyard(player2, "Sawblade Scamp");
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The caster's creatures remain able to block")
    void doesNotRestrictControllersCreatures() {
        Permanent own = addCreatureReady(player1, new ContagiousVorrac());
        Permanent opponent = addCreatureReady(player2, new ContagiousVorrac());

        castHazardousBlast();

        assertThat(bls.canBlockAttacker(gd, own, opponent, gd.playerBattlefields.get(player1.getId())))
                .isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new ContagiousVorrac());
        Permanent blocker = addCreatureReady(player2, new ContagiousVorrac());

        castHazardousBlast();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player2.getId())))
                .isTrue();
    }

    private void castHazardousBlast() {
        harness.castFromHand(player1, new HazardousBlast(), "{3}{R}");
        harness.passBothPriorities();
    }
}
