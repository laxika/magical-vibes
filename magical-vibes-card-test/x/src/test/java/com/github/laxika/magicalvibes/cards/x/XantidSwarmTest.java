package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XantidSwarm.class, AvenFarseer.class, Stifle.class, JaceTheMindSculptor.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class XantidSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger prevents the defending player from casting spells this turn")
    void attackTriggerSilencesDefendingPlayer() {
        addCreatureReady(player1, new XantidSwarm());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());

        harness.setHand(player2, List.of(new AvenFarseer()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack trigger does not prevent its controller from casting spells")
    void attackTriggerDoesNotSilenceController() {
        addCreatureReady(player1, new XantidSwarm());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new AvenFarseer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Attack trigger fires even when Xantid Swarm is blocked")
    void attackTriggerFiresWhenBlocked() {
        addCreatureReady(player1, new XantidSwarm());
        Permanent blocker = addCreatureReady(player2, new AvenFarseer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveAllTriggers();

        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());
    }

    @Test
    @DisplayName("Attack restriction ends at the end of the turn")
    void attackRestrictionEndsAtEndOfTurn() {
        addCreatureReady(player1, new XantidSwarm());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playersSilencedThisTurn).contains(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new AvenFarseer()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(gd.playersSilencedThisTurn).doesNotContain(player2.getId());
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    void defendingPlayerCanCounterAttackTriggerBeforeItResolves() {
        addCreatureReady(player1, new XantidSwarm());
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playersSilencedThisTurn).isEmpty();
        var triggerCardId = gd.stack.getLast().getCard().getId();
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, triggerCardId);
        resolveAllTriggers();

        assertThat(gd.playersSilencedThisTurn).isEmpty();
    }

    @Test
    void planeswalkerLeavingBeforeResolutionDoesNotLoseDefendingPlayer() {
        addCreatureReady(player1, new XantidSwarm());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, jace.getId()));

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(jace);
        gd.playerGraveyards.get(player2.getId()).add(jace.getCard());
        resolveAllTriggers();

        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());
    }

    @Test
    void attackingBattleSilencesProtectorRatherThanController() {
        addCreatureReady(player1, new XantidSwarm());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
        resolveAllTriggers();

        assertThat(gd.playersSilencedThisTurn).containsOnly(player2.getId());
    }
}
