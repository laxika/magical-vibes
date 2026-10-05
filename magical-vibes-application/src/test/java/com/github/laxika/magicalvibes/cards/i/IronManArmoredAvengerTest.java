package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronManArmoredAvenger.class, GrizzlyBears.class, Island.class,
        HolyStrength.class, LeoninScimitar.class})
class IronManArmoredAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing a card puts a +1/+1 counter on a target creature")
    void drawingPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new IronManArmoredAvenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The draw trigger only allows creature targets")
    void drawTriggerOnlyAllowsCreatureTargets() {
        harness.addToBattlefield(player1, new IronManArmoredAvenger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        Permanent land = gd.playerBattlefields.get(player1.getId()).get(2);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking gives flying to other attacking modified creatures")
    void attackingGivesFlyingToOtherAttackingModifiedCreatures() {
        addCreatureReady(player1, new IronManArmoredAvenger());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedAttacker, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger's flying grant wears off at end of turn")
    void flyingGrantWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new IronManArmoredAvenger());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Iron Man can receive the counter from its own draw trigger")
    void drawTriggerCanTargetIronMan() {
        Permanent ironMan = harness.addToBattlefieldAndReturn(player1, new IronManArmoredAvenger());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ironMan.getId());
        resolveAllTriggers();

        assertThat(ironMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's draw does not trigger Iron Man")
    void opponentDrawDoesNotPutCounters() {
        Permanent ironMan = harness.addToBattlefieldAndReturn(player1, new IronManArmoredAvenger());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ironMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Drawing multiple cards outside the draw step triggers once for each card")
    void eachCardDrawnTriggersOnAnOpponentsTurn() {
        Permanent ironMan = harness.addToBattlefieldAndReturn(player1, new IronManArmoredAvenger());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, ironMan.getId());
        }
        resolveAllTriggers();

        assertThat(ironMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment and friendly Auras modify attackers, but opposing Auras do not")
    void equipmentAndAuraControllerDetermineModification() {
        addCreatureReady(player1, new IronManArmoredAvenger());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent friendlyAuraCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingAuraCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(equipped.getId());
        Permanent friendlyAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        friendlyAura.setAttachedTo(friendlyAuraCreature.getId());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        opposingAura.setAttachedTo(opposingAuraCreature.getId());

        declareAttackers(player1, List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, equipped, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, friendlyAuraCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingAuraCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The set of creatures granted flying stays fixed after resolution")
    void flyingPersistsAfterModificationAndAttackingEnd() {
        addCreatureReady(player1, new IronManArmoredAvenger());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        modified.setAttacking(false);
        unmodified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modified, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodified, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Modification is checked when the attack trigger resolves")
    void modificationIsCheckedAtResolution() {
        addCreatureReady(player1, new IronManArmoredAvenger());
        Permanent losesCounter = addCreatureReady(player1, new GrizzlyBears());
        Permanent gainsCounter = addCreatureReady(player1, new GrizzlyBears());
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));
        assertThat(gd.stack).isNotEmpty();
        losesCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        gainsCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, losesCounter, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, gainsCounter, Keyword.FLYING)).isTrue();
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
