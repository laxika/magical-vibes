package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PitilessGorgon;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VraskaGolgariQueen.class, Forest.class, GrizzlyBears.class, LlanowarElves.class,
        SiegeWurm.class, PitilessGorgon.class})
class VraskaGolgariQueenTest extends BaseCardTest {

    @Test
    @DisplayName("+2 sacrifices another permanent, gains life, and draws a card")
    void plusTwoSacrificesGainsLifeAndDraws() {
        Permanent vraska = addReadyVraska(5);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("+2 may be declined")
    void plusTwoMayBeDeclined() {
        Permanent vraska = addReadyVraska(5);
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("+2 only offers another permanent to sacrifice")
    void plusTwoOnlyOffersAnotherPermanent() {
        Permanent vraska = addReadyVraska(5);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.validIds()).doesNotContain(vraska.getId());
    }

    @Test
    @DisplayName("-3 destroys a nonland permanent with mana value 3 or less")
    void minusThreeDestroysSmallNonlandPermanent() {
        Permanent vraska = addReadyVraska(5);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("-3 rejects a land target")
    void minusThreeRejectsLand() {
        addReadyVraska(5);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-9 emblem makes a player lose when a controlled creature deals combat damage")
    void minusNineEmblemMakesPlayerLoseFromCombatDamage() {
        addReadyVraska(9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveCombatAndTriggers(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("-9 emblem does not trigger from an opponent's creature")
    void minusNineEmblemDoesNotTriggerFromOpponentsCreature() {
        addReadyVraska(9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(bears)));
        resolveCombatAndTriggers(player2);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void plusTwoCompletesLifeGainAndDrawDuringOriginalResolution() {
        addReadyVraska(5);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handlePermanentChosen(player1, forest.getId()));

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusTwoWithNoOtherPermanentDoesNotGainLifeOrDraw() {
        Permanent vraska = addReadyVraska(5);
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Vraska, Golgari Queen");
    }

    @Test
    void minusThreeRejectsManaValueAboveThree() {
        addReadyVraska(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VraskaGolgariQueen());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusThreeDestroysManaValueExactlyThreeWithLastLoyalty() {
        addReadyVraska(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PitilessGorgon());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vraska, Golgari Queen");
        harness.assertInGraveyard(player2, "Pitiless Gorgon");
    }

    @Test
    void emblemTriggersWhenTramplingCreatureDiesInSameDamageStep() {
        addReadyVraska(9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent wurm = addCreatureReady(player1, new SiegeWurm());
        Permanent gorgon = addCreatureReady(player2, new PitilessGorgon());
        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wurm)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(gorgon),
                gd.playerBattlefields.get(player1.getId()).indexOf(wurm))));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(wurm),
                Map.of(gorgon.getId(), 2, player2.getId(), 3));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Siege Wurm");
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private Permanent addReadyVraska(int loyalty) {
        Permanent perm = new Permanent(new VraskaGolgariQueen());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void resolveCombatAndTriggers(Player activePlayer) {
        resolveCombat(activePlayer);
        resolveAllTriggers();
    }
}