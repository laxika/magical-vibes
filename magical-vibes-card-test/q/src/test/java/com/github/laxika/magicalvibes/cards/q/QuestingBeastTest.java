package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuestingBeast.class, GrizzlyBears.class, HillGiant.class, Fog.class, ChandraNalaar.class})
class QuestingBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Questing Beast can't be blocked by a creature with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(beast);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Questing Beast can be blocked by a creature with power 3 or greater")
    void canBeBlockedByHighPowerCreature() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(beast);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage from creatures you control can't be prevented")
    void combatDamageCannotBePrevented() {
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        beast.setAttacking(true);
        bear.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Combat damage to an opponent also damages that player's planeswalker")
    void damagesDefendingPlaneswalkerForCombatDamageAmount() {
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        Permanent planeswalker = addTestPlaneswalker(player2, 6);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private Permanent addTestPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }

    @Test
    @DisplayName("Questing Beast does not make opposing creatures' combat damage unpreventable")
    void opposingCombatDamageCanStillBePrevented() {
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        addCreatureReady(player1, new QuestingBeast());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only planeswalkers controlled by the damaged opponent are legal targets")
    void triggerExcludesCreaturesAndOwnPlaneswalker() {
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        Permanent ownPlaneswalker = addTestPlaneswalker(player1, 6);
        Permanent opposingPlaneswalker = addTestPlaneswalker(player2, 6);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(opposingPlaneswalker.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
        harness.handlePermanentChosen(player1, opposingPlaneswalker.getId());
        harness.passBothPriorities();

        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("No target is chosen when the damaged opponent controls no planeswalker")
    void ownPlaneswalkerDoesNotProvideLegalTarget() {
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        Permanent ownPlaneswalker = addTestPlaneswalker(player1, 6);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("The planeswalker trigger resolves after Questing Beast leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        Permanent planeswalker = addTestPlaneswalker(player2, 6);
        resolveCombat();
        harness.withAutoStop(gd.currentStep,
                () -> harness.handlePermanentChosen(player1, planeswalker.getId()));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(beast);
        gd.playerGraveyards.get(player1.getId()).add(beast.getCard());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger cannot damage a planeswalker no longer controlled by the damaged opponent")
    void triggerDoesNotDamagePlaneswalkerAfterControlChanges() {
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        Permanent planeswalker = addTestPlaneswalker(player2, 6);
        resolveCombat();
        harness.withAutoStop(gd.currentStep,
                () -> harness.handlePermanentChosen(player1, planeswalker.getId()));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Haste allows Questing Beast to attack immediately without tapping due to vigilance")
    void attacksWhileSummoningSickWithoutTapping() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new QuestingBeast());
        beast.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(beast.isAttacking()).isTrue();
        assertThat(beast.isTapped()).isFalse();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The blocking restriction checks current power rather than printed power")
    void boostedTwoPowerCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch kills a legal blocker even when combat damage prevention is active")
    void deathtouchKillsLargerBlockerThroughFog() {
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent beast = addCreatureReady(player1, new QuestingBeast());
        beast.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(beast);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
