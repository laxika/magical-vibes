package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.m.MigratoryGreathorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceSymbiote.class, AlmightyBrushwagg.class, MigratoryGreathorn.class})
class EssenceSymbioteTest extends BaseCardTest {

    @Test
    @DisplayName("A controlled creature mutating gets a counter and its controller gains 2 life")
    void controlledCreatureMutatingGetsCounterAndLifeGain() {
        Permanent symbiote = addCreatureReady(player1, new EssenceSymbiote());
        Permanent mutatedCreature = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setLife(player1, 20);

        mutate(mutatedCreature, player1.getId());

        assertThat(mutatedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An opponent's creature mutating does not trigger Essence Symbiote")
    void opponentCreatureMutatingDoesNotTrigger() {
        Permanent symbiote = addCreatureReady(player1, new EssenceSymbiote());
        Permanent opposingCreature = addCreatureReady(player2, new AlmightyBrushwagg());
        harness.setLife(player1, 20);

        mutate(opposingCreature, player2.getId());

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Essence Symbiote triggers when it mutates itself")
    void selfMutationGetsCounterAndLife() {
        Permanent symbiote = addCreatureReady(player1, new EssenceSymbiote());
        harness.setLife(player1, 20);

        mutate(symbiote, player1.getId());

        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Each Essence Symbiote triggers for each mutation")
    void multipleSymbiotesAndRepeatedMutations() {
        addCreatureReady(player1, new EssenceSymbiote());
        addCreatureReady(player1, new EssenceSymbiote());
        Permanent creature = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setLife(player1, 20);

        mutate(creature, player1.getId());
        mutate(creature, player1.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Life is gained even if the mutated creature leaves before resolution")
    void lifeGainWithoutMutatedCreature() {
        addCreatureReady(player1, new EssenceSymbiote());
        Permanent creature = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setLife(player1, 20);

        collectMutationTriggers(creature, player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The ability resolves after Essence Symbiote leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent symbiote = addCreatureReady(player1, new EssenceSymbiote());
        Permanent creature = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setLife(player1, 20);

        collectMutationTriggers(creature, player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(symbiote);
        gd.playerGraveyards.get(player1.getId()).add(symbiote.getCard());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    private void collectMutationTriggers(Permanent creature, java.util.UUID controllerId) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, creature, List.of(creature.getCard()), controllerId));
    }

    @Test
    @DisplayName("Resolving a mutating creature spell triggers Essence Symbiote")
    void resolvingMutateSpellGivesCounterAndLife() {
        addCreatureReady(player1, new EssenceSymbiote());
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MigratoryGreathorn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, host.getId());
        resolveAllTriggers();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    private void mutate(Permanent creature, java.util.UUID controllerId) {
        collectMutationTriggers(creature, controllerId);
        resolveAllTriggers();
    }
}
