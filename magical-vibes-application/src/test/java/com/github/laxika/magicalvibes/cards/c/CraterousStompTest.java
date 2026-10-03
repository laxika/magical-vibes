package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CraterousStomp.class, GrizzlyBears.class, HillGiant.class, SengirVampire.class,
        ElvishWarrior.class, Humble.class})
class CraterousStompTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage and permanently makes the target's other creatures Cowards")
    void damagesTargetAndMakesOtherCreaturesCowards() {
        Permanent target = addCreatureReady(player2, new SengirVampire());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castStomp(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).contains(CardSubtype.COWARD);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).doesNotContain(CardSubtype.COWARD);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).doesNotContain(CardSubtype.COWARD);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).contains(CardSubtype.COWARD);
    }

    @Test
    @DisplayName("An affected creature cannot block Giants")
    void affectedCreatureCannotBlockGiant() {
        Permanent target = addCreatureReady(player2, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new HillGiant());

        castStomp(target);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Giants or Warriors");
    }

    @Test
    @DisplayName("An affected creature can still block a non-Giant, non-Warrior")
    void affectedCreatureCanBlockOtherCreatures() {
        Permanent target = addCreatureReady(player2, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        castStomp(target);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can target only a creature an opponent controls")
    void targetMustBeOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CraterousStomp()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void lethalDamageStillMakesOtherCreaturesCowards() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new SengirVampire());

        castStomp(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).contains(CardSubtype.VAMPIRE, CardSubtype.COWARD);
    }

    @Test
    void affectedCreatureCannotBlockWarrior() {
        Permanent target = addCreatureReady(player2, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());

        castStomp(target);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Giants or Warriors");
    }

    @Test
    void creaturesEnteringAfterResolutionAreUnaffected() {
        Permanent target = addCreatureReady(player2, new SengirVampire());
        castStomp(target);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new HillGiant());

        assertThat(gqs.effectiveCreatureSubtypes(gd, blocker)).doesNotContain(CardSubtype.COWARD);
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void losingAbilitiesRemovesGrantedBlockingRestriction() {
        Permanent target = addCreatureReady(player2, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        castStomp(target);

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(gqs.effectiveCreatureSubtypes(gd, blocker)).contains(CardSubtype.COWARD);
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void illegalTargetAtResolutionPreventsAllEffects() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new SengirVampire());
        harness.setHand(player1, List.of(new CraterousStomp()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).doesNotContain(CardSubtype.COWARD);
        harness.assertInGraveyard(player1, "Craterous Stomp");
    }

    private void castStomp(Permanent target) {
        harness.setHand(player1, List.of(new CraterousStomp()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
