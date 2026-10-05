package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvigoratingHotSpring.class, BearerOfMemory.class, AncestralKatana.class, ShortCircuit.class})
class InvigoratingHotSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourCounters() {
        Permanent spring = castSpring();

        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gives haste to modified creatures you control")
    void givesHasteToModifiedCreatures() {
        harness.addToBattlefield(player1, new InvigoratingHotSpring());
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent unmodified = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modified, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodified, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Removes a counter to put one on a creature you control")
    void movesCounterToControlledCreature() {
        Permanent spring = castSpring();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate only once each turn and cannot target an opponent's creature")
    void activationRestrictions() {
        Permanent spring = castSpring();
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, null, ownTarget.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void onlyControlledModifiedCreaturesGainHasteAndLoseItWhenUnmodified() {
        harness.addToBattlefield(player1, new InvigoratingHotSpring());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new BearerOfMemory());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.HASTE)).isFalse();
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isFalse();
    }

    @Test
    void ownAuraModifiesCreatureButOpposingAuraDoesNot() {
        harness.addToBattlefield(player1, new InvigoratingHotSpring());
        Permanent ownEnchanted = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent opponentEnchanted = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new ShortCircuit());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new ShortCircuit());
        ownAura.setAttachedTo(ownEnchanted.getId());
        opposingAura.setAttachedTo(opponentEnchanted.getId());

        assertThat(gqs.hasKeyword(gd, ownEnchanted, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentEnchanted, Keyword.HASTE)).isFalse();
    }

    @Test
    void equipmentModifiesCreatureRegardlessOfEquipmentController() {
        harness.addToBattlefield(player1, new InvigoratingHotSpring());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateWithoutCounter() {
        Permanent spring = castSpring();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        spring.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent spring = castSpring();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void cannotActivateWithSpellOnStack() {
        Permanent spring = castSpring();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.castFromHand(player1, new BearerOfMemory(), "{2}{G}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void counterIsPaidBeforeResolutionAndHasteAppearsOnlyAfterResolution() {
        Permanent spring = castSpring();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateOnOpponentsTurn() {
        Permanent spring = castSpring();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spring.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void eachSpringCanActivateOnceInSameTurn() {
        Permanent first = castSpring();
        Permanent second = castSpring();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent castSpring() {
        InvigoratingHotSpring springCard = new InvigoratingHotSpring();

        harness.castFromHand(player1, springCard, "{1}{R}{G}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
