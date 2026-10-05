package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GuidingSpirit;
import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.q.QuirionDruid;
import com.github.laxika.magicalvibes.cards.c.CoralAtoll;
import com.github.laxika.magicalvibes.cards.s.SpittingDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({KatabaticWinds.class, SpittingDrake.class, PantherWarriors.class, GuidingSpirit.class,
        QuirionDruid.class, CoralAtoll.class})
class KatabaticWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Flying creature cannot attack while Katabatic Winds is on the battlefield")
    void flyingCreatureCannotAttack() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent flyer = addCreatureReady(player1, new SpittingDrake());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);
        assertThatThrownBy(() -> declareAttackers(List.of(idx)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-flying creature attacks normally while Katabatic Winds is on the battlefield")
    void nonFlyingCreatureCanAttack() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        harness.setLife(player2, 20);
        Permanent ground = addCreatureReady(player1, new PantherWarriors());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(ground);
        declareAttackers(List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Flying creature cannot block while Katabatic Winds is on the battlefield")
    void flyingCreatureCannotBlock() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent attacker = addCreatureReady(player1, new PantherWarriors());
        attacker.setAttacking(true);
        Permanent flyer = addCreatureReady(player2, new SpittingDrake());

        prepareDeclareBlockers();

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int flyerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(flyer);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(flyerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-flying creature blocks normally while Katabatic Winds is on the battlefield")
    void nonFlyingCreatureCanBlock() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent attacker = addCreatureReady(player1, new PantherWarriors());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PantherWarriors());

        prepareDeclareBlockers();
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Flying creature cannot activate a {T} ability")
    void flyingCreatureCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent spirit = addCreatureReady(player2, new GuidingSpirit());
        int spiritIdx = gd.playerBattlefields.get(player2.getId()).indexOf(spirit);

        assertThatThrownBy(() -> harness.activateAbility(player2, spiritIdx, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Flying creature can still activate a non-tap ability")
    void flyingCreatureCanActivateNonTapAbility() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent drake = addCreatureReady(player1, new SpittingDrake());
        int drakeIdx = gd.playerBattlefields.get(player1.getId()).indexOf(drake);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, drakeIdx, null, null);
        harness.passBothPriorities();

        assertThat(drake.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-flying creature can still activate a {T} ability")
    void nonFlyingCreatureCanActivateTapAbility() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent druid = addCreatureReady(player1, new QuirionDruid());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CoralAtoll());
        int druidIdx = gd.playerBattlefields.get(player1.getId()).indexOf(druid);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, druidIdx, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(druid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Restriction lifts when Katabatic Winds leaves the battlefield")
    void restrictionLiftsWhenKatabaticWindsLeaves() {
        Permanent winds = harness.addToBattlefieldAndReturn(player1, new KatabaticWinds());
        harness.setLife(player2, 20);
        Permanent flyer = addCreatureReady(player1, new SpittingDrake());

        gd.playerBattlefields.get(player1.getId()).remove(winds);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);
        declareAttackers(List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Katabatic Winds phases out during its controller's untap step and phases back in the next one")
    void phasesOutAndInOnControllersUntapSteps() {
        Permanent winds = harness.addToBattlefieldAndReturn(player1, new KatabaticWinds());

        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(winds);

        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(winds);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(winds);

        advanceTurn();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(winds);

        advanceTurn();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(winds);
    }

    @Test
    @DisplayName("Flying creatures can attack while Katabatic Winds is phased out")
    void flyingCreatureCanAttackWhileWindsIsPhasedOut() {
        Permanent winds = harness.addToBattlefieldAndReturn(player1, new KatabaticWinds());
        Permanent flyer = addCreatureReady(player1, new SpittingDrake());
        harness.setLife(player2, 20);

        harness.performUntapStep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(winds);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(flyer)));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Katabatic Winds also prevents opposing flying creatures from attacking")
    void opposingFlyingCreatureCannotAttack() {
        harness.addToBattlefield(player1, new KatabaticWinds());
        Permanent flyer = addCreatureReady(player2, new SpittingDrake());

        assertThatThrownBy(() -> declareAttackers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(flyer))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying creatures can block while Katabatic Winds is phased out")
    void flyingCreatureCanBlockWhileWindsIsPhasedOut() {
        Permanent winds = harness.addToBattlefieldAndReturn(player1, new KatabaticWinds());
        Permanent attacker = addCreatureReady(player1, new PantherWarriors());
        Permanent blocker = addCreatureReady(player2, new SpittingDrake());

        harness.performUntapStep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(winds);
        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Flying creature tap abilities become legal while Winds is phased out and are locked again when it phases in")
    void tapAbilityRestrictionFollowsPhasing() {
        Permanent winds = harness.addToBattlefieldAndReturn(player1, new KatabaticWinds());
        Permanent spirit = addCreatureReady(player1, new GuidingSpirit());

        advanceToUpkeep(player1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(winds);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spirit),
                null, player2.getId());
        harness.passBothPriorities();
        assertThat(spirit.isTapped()).isTrue();

        advanceToUpkeep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(winds);
        assertThat(spirit.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(spirit), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UNTAP);
    }
}
