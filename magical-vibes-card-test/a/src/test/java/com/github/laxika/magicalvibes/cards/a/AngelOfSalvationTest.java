package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BladeOfTheSixthPride;
import com.github.laxika.magicalvibes.cards.s.SproutSwarm;
import com.github.laxika.magicalvibes.cards.v.VeilstoneAmulet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfSalvation.class, BladeOfTheSixthPride.class, ArcBlade.class,
        SproutSwarm.class, VeilstoneAmulet.class})
class AngelOfSalvationTest extends BaseCardTest {

    @Test
    void etbPreventionIsDividedBetweenPlayerAndCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 2, creature.getId(), 3);

        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ArcBlade(), new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    void etbPreventionDoesNotProtectUnassignedTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 5);

        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Blade of the Sixth Pride");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void etbPreventionWithNoAssignmentsDoesNothing() {
        gd.pendingETBDamageAssignments = Map.of();

        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void preventionIsConsumedAcrossSuccessiveDamageEvents() {
        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 5);
        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ArcBlade(), new ArcBlade(), new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 15);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 19);
    }

    @Test
    void overlappingAngelTriggersKeepTheirOwnDivisions() {
        gd.pendingETBDamageAssignments = Map.of(player1.getId(), 5);
        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");
        harness.passBothPriorities();

        gd.pendingETBDamageAssignments = Map.of(player2.getId(), 5);
        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ArcBlade(), new ArcBlade()));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void targetThatBecomesIllegalGetsNoPrevention() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BladeOfTheSixthPride());
        harness.addToBattlefield(player2, new VeilstoneAmulet());
        gd.pendingETBDamageAssignments = Map.of(creature.getId(), 3, player2.getId(), 2);
        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");
        harness.passBothPriorities();

        harness.castFromHand(player2, new SproutSwarm(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        addCreatureReady(player1, new BladeOfTheSixthPride());
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Blade of the Sixth Pride");
    }

    @Test
    void canCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new AngelOfSalvation(), "{6}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(AngelOfSalvation.class);
    }

    @Test
    void castsWithConvoke() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSixthPride());
        convoker.setSummoningSick(false);

        harness.setHand(player1, List.of(new AngelOfSalvation()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel of Salvation");
    }

    @Test
    void summoningSickCreatureCanConvoke() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new BladeOfTheSixthPride());
        convoker.setSummoningSick(true);
        harness.setHand(player1, List.of(new AngelOfSalvation()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convoker.getId()));
        harness.passBothPriorities();

        assertThat(convoker.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Angel of Salvation");
    }

    @Test
    void flyingAllowsAttackingOverGroundCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AngelOfSalvation());
        harness.addToBattlefield(player2, new BladeOfTheSixthPride());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
