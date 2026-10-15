package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.g.GideonChampionOfJustice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrzhovAdvokist.class, GrizzlyBears.class, DeadlyInsect.class, GideonChampionOfJustice.class})
class OrzhovAdvokistTest extends BaseCardTest {

    @Test
    void eachPlayerMayPutCountersOnTheirCreatureAndThatPlayersCreaturesCannotAttackUntilControllerNextTurn() {
        harness.addToBattlefield(player1, new OrzhovAdvokist());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent player2OtherCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(player2Creature.getId(), player2OtherCreature.getId());
        harness.handlePermanentChosen(player2, player2Creature.getId());
        harness.passBothPriorities();

        assertThat(player1Creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(player2Creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(player2OtherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThat(gd.floatingEffects)
                .noneMatch(floating -> floating.effect() instanceof CreaturesCantAttackControllerUnlessPredicateEffect);
    }

    @Test
    void countersAndRestrictionApplyDuringTheOriginalAbilityWithoutAdditionalStackEntries() {
        harness.addToBattlefield(player1, new OrzhovAdvokist());
        Permanent creature = addCreatureReady(player2, new OrzhovAdvokist());
        Permanent otherCreature = addCreatureReady(player2, new OrzhovAdvokist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> declareAttackers(player2, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aCreatureWithShroudCanBeChosenBecauseTheAbilityDoesNotTarget() {
        harness.addToBattlefield(player1, new OrzhovAdvokist());
        Permanent insect = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new OrzhovAdvokist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(insect.getId(), otherCreature.getId());
        harness.handlePermanentChosen(player2, insect.getId());
        assertThat(insect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void decliningDoesNotRestrictAttacking() {
        harness.addToBattlefield(player1, new OrzhovAdvokist());
        Permanent creature = addCreatureReady(player2, new OrzhovAdvokist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    void controllerCanAcceptAndOpponentWithoutCreaturesIsSkipped() {
        Permanent advokist = harness.addToBattlefieldAndReturn(player1, new OrzhovAdvokist());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(advokist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictionProtectsPlaneswalkersAndNewCreaturesRemainRestrictedAfterSourceAndRecipientLeave() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OrzhovAdvokist());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GideonChampionOfJustice());
        Permanent recipient = addCreatureReady(player2, new OrzhovAdvokist());
        addCreatureReady(player2, new OrzhovAdvokist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, recipient.getId());
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).remove(recipient);
        Permanent newCreature = addCreatureReady(player2, new OrzhovAdvokist());

        assertThat(als.canAttackDefender(gd, newCreature, player1.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, newCreature, planeswalker.getId())).isFalse();
        gd.expireFloatingEffectsAtTurnStart(player2.getId());
        assertThat(als.canAttackDefender(gd, newCreature, player1.getId())).isFalse();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThat(als.canAttackDefender(gd, newCreature, player1.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, newCreature, planeswalker.getId())).isTrue();
    }
}
