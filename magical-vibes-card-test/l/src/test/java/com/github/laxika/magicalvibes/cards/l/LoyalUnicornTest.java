package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalUnicorn.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class LoyalUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant grants other creatures vigilance until end of turn")
    void lieutenantGrantsVigilanceUntilEndOfTurn() {
        addCommander(player1);
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Lieutenant does not trigger without controlling a commander")
    void lieutenantDoesNotTriggerWithoutCommander() {
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Lieutenant prevents combat damage to creatures you control")
    void lieutenantPreventsCombatDamageToYourCreatures() {
        addCommander(player1);
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        advanceToCombatAndResolve(player1);
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void lieutenantDoesNotPreventNoncombatDamage() {
        addCommander(player1);
        Permanent unicorn = addCreatureReady(player1, new LoyalUnicorn());
        advanceToCombatAndResolve(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, unicorn.getId());

        assertThat(unicorn.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        addCommander(player1);
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void commanderMustStillBeControlledWhenTriggerResolves() {
        addCommander(player1);
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent other = addCreatureReady(player1, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p ->
                p.getOriginalCard().getId().equals(gd.playerCommanders.get(player1.getId()).getFirst().getId()));

        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void laterCreaturesAreProtectedButDoNotGainVigilance() {
        addCommander(player1);
        addCreatureReady(player1, new LoyalUnicorn());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        advanceToCombatAndResolve(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.VIGILANCE)).isFalse();
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void resolvedEffectsPersistAfterCommanderAndUnicornLeave() {
        addCommander(player1);
        Permanent unicorn = addCreatureReady(player1, new LoyalUnicorn());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        advanceToCombatAndResolve(player1);
        gd.playerBattlefields.get(player1.getId()).remove(unicorn);
        gd.playerBattlefields.get(player1.getId()).removeIf(p ->
                p.getOriginalCard().getId().equals(gd.playerCommanders.get(player1.getId()).getFirst().getId()));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.VIGILANCE)).isTrue();
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    private void addCommander(Player player) {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player.getId(), commander);
        harness.addToBattlefield(player, commander);
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
