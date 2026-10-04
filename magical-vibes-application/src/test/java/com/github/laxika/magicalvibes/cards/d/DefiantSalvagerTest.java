package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherPoisoner;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RenegadeMap;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefiantSalvager.class, AetherPoisoner.class, Ornithopter.class, RenegadeMap.class})
class DefiantSalvagerTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactAndPutsACounterOnItself() {
        Permanent salvager = addCreatureReady(player1, new DefiantSalvager());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());
        prepareForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(salvager.getId(), artifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(salvager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(salvager).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getOriginalCard());
    }

    @Test
    void sacrificesACreatureAndPutsACounterOnItself() {
        Permanent salvager = addCreatureReady(player1, new DefiantSalvager());
        Permanent creature = addCreatureReady(player1, new AetherPoisoner());
        prepareForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(salvager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(salvager).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getOriginalCard());
    }

    @Test
    void canOnlyBeActivatedAtSorcerySpeed() {
        addCreatureReady(player1, new DefiantSalvager());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    void sacrificeIsPaidBeforeTheCounterAbilityResolves() {
        Permanent salvager = harness.addToBattlefieldAndReturn(player1, new DefiantSalvager());
        salvager.tap();
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new RenegadeMap());
        prepareForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(salvager.getId(), artifactCreature.getId());
        harness.handlePermanentChosen(player1, artifactCreature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifactCreature.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifactCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
        assertThat(salvager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(salvager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canSacrificeItselfWithoutPuttingACounterOnAnotherSalvager() {
        Permanent salvager = addCreatureReady(player1, new DefiantSalvager());
        Permanent otherSalvager = addCreatureReady(player1, new DefiantSalvager());
        prepareForSorcerySpeed(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, salvager.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(salvager);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(salvager.getOriginalCard());
        harness.passBothPriorities();

        assertThat(otherSalvager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addCreatureReady(player1, new DefiantSalvager());
        prepareForSorcerySpeed(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void canActivateRepeatedlyDuringPostcombatMainPhase() {
        Permanent salvager = addCreatureReady(player1, new DefiantSalvager());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());
        prepareForSorcerySpeed(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(salvager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getOriginalCard(), second.getOriginalCard());
    }

    private void prepareForSorcerySpeed(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
