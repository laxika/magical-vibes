package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbzanSkycaptain;
import com.github.laxika.magicalvibes.cards.s.ShockmawDragon;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromokaTheEternal.class, AbzanSkycaptain.class, ShockmawDragon.class, TurnToFrog.class})
class DromokaTheEternalTest extends BaseCardTest {

    @Test
    void attackingDragonBolstersTheLeastToughCreature() {
        addCreatureReady(player1, new DromokaTheEternal());
        Permanent creature = addCreatureReady(player1, new AbzanSkycaptain());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackingNonDragonDoesNotTrigger() {
        addCreatureReady(player1, new DromokaTheEternal());
        Permanent creature = addCreatureReady(player1, new AbzanSkycaptain());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsDragonDoesNotTrigger() {
        addCreatureReady(player1, new DromokaTheEternal());
        Permanent dragon = addCreatureReady(player2, new ShockmawDragon());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dromokaBolstersItselfWhenItIsTheOnlyCreature() {
        Permanent dromoka = addCreatureReady(player1, new DromokaTheEternal());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(dromoka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void anotherDragonTriggersWhileDromokaStaysBack() {
        addCreatureReady(player1, new DromokaTheEternal());
        addCreatureReady(player1, new ShockmawDragon());
        Permanent creature = addCreatureReady(player1, new AbzanSkycaptain());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void eachAttackingDragonBolstersSeparately() {
        addCreatureReady(player1, new DromokaTheEternal());
        Permanent dragon = addCreatureReady(player1, new ShockmawDragon());
        Permanent creature = addCreatureReady(player1, new AbzanSkycaptain());

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), dragon.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tiedLeastToughnessAllowsChoiceAndIgnoresOpponentsCreatures() {
        addCreatureReady(player1, new DromokaTheEternal());
        Permanent first = addCreatureReady(player1, new AbzanSkycaptain());
        Permanent second = addCreatureReady(player1, new AbzanSkycaptain());
        addCreatureReady(player2, new AbzanSkycaptain());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void bolsterUsesCreaturesPresentAtResolutionEvenAfterSourceLeaves() {
        Permanent dromoka = addCreatureReady(player1, new DromokaTheEternal());
        declareAttackers(List.of(0));

        gd.playerBattlefields.get(player1.getId()).remove(dromoka);
        gd.playerGraveyards.get(player1.getId()).add(dromoka.getCard());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AbzanSkycaptain());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void bolsterDoesNothingWhenNoCreaturesRemain() {
        Permanent dromoka = addCreatureReady(player1, new DromokaTheEternal());
        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(dromoka);
        gd.playerGraveyards.get(player1.getId()).add(dromoka.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void dromokaWithNoAbilitiesDoesNotTriggerForAnotherDragon() {
        Permanent dromoka = addCreatureReady(player1, new DromokaTheEternal());
        addCreatureReady(player1, new ShockmawDragon());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, dromoka.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(dromoka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void dragonChangedIntoFrogDoesNotTriggerDromoka() {
        addCreatureReady(player1, new DromokaTheEternal());
        Permanent dragon = addCreatureReady(player1, new ShockmawDragon());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, dragon.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
