package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirbenderAscension.class, TurtleDuck.class, Island.class})
class AirbenderAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Airbends up to one target creature when it enters")
    void airbendsTargetCreatureOnEntry() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        castAscension(bears.getId());

        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("The airbend target must be a creature")
    void airbendRejectsNonCreatureTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new AirbenderAscension()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("A creature you control entering puts a quest counter on it")
    void creatureEnteringAddsQuestCounter() {
        Permanent ascension = addAscension();
        harness.setHand(player1, List.of(new TurtleDuck()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Four quest counters flicker a target creature at your end step")
    void fourQuestCountersFlickerOwnCreatureAtEndStep() {
        Permanent ascension = addAscension();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        ascension.setCounterCount(CounterType.QUEST, 4);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Turtle-Duck"));
    }

    @Test
    void mayEnterWithoutAirbendingACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());

        castAscension(null);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Airbender Ascension");
    }

    @Test
    void opponentCreatureEnteringDoesNotAddQuestCounter() {
        Permanent ascension = addAscension();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TurtleDuck()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void airbentCreatureOwnerMayCastItForTwoGenericMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        castAscension(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromExile(player2, creature.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Turtle-Duck");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
    }

    @Test
    void flickeredStolenCreatureReturnsToItsOwner() {
        Permanent ascension = addAscension();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), creature,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, "Test setup"));
        ascension.setCounterCount(CounterType.QUEST, 4);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Turtle-Duck");
        harness.assertOnBattlefield(player2, "Turtle-Duck");
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(4);
    }

    @Test
    void fewerThanFourCountersDoesNotTriggerAtEndStep() {
        Permanent ascension = addAscension();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        ascension.setCounterCount(CounterType.QUEST, 3);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void opponentEndStepDoesNotTriggerFlicker() {
        Permanent ascension = addAscension();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        ascension.setCounterCount(CounterType.QUEST, 4);
        harness.forceActivePlayer(player2);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void counterThresholdIsCheckedAgainOnResolution() {
        Permanent ascension = addAscension();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        ascension.setCounterCount(CounterType.QUEST, 4);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, creature.getId());
        ascension.setCounterCount(CounterType.QUEST, 3);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(3);
    }

    @Test
    void flickeredCreatureEnteringAddsAnotherQuestCounter() {
        Permanent ascension = addAscension();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        ascension.setCounterCount(CounterType.QUEST, 5);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ascension.getCounterCount(CounterType.QUEST)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertOnBattlefield(player1, "Turtle-Duck");
    }

    private void castAscension(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new AirbenderAscension()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addAscension() {
        return harness.addToBattlefieldAndReturn(player1, new AirbenderAscension());
    }
}
