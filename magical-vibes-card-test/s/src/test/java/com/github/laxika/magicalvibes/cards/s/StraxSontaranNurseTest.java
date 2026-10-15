package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StraxSontaranNurse.class, GrizzlyBears.class, MindStone.class})
class StraxSontaranNurseTest extends BaseCardTest {

    @Test
    void sacrificesAnArtifactThenReflexivelyFightsAnotherCreature() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .containsAnyOf(ownCreature.getId(), opposingCreature.getId())
                .doesNotContain(strax.getId());

        Permanent selected = choice.validIds().contains(ownCreature.getId()) ? ownCreature : opposingCreature;
        harness.handlePermanentChosen(player1, selected.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(selected == ownCreature ? player1.getId() : player2.getId()))
                .doesNotContain(selected);
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void combatDamageToACreatureAddsOneCounterRegardlessOfDamageAmount() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void anotherCreaturesDamageDoesNotPutCountersOnStrax() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void damageToAPlayerDoesNotPutCountersOnStrax() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activationWithNoOtherCreaturesStillPaysCostsButDoesNotFight() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(strax.isTapped()).isTrue();
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalFightDamageKillsStraxBeforeItsCounterCanSaveIt() {
        Permanent strax = addCreatureReady(player1, new StraxSontaranNurse());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        Permanent selected = choice.validIds().contains(ownCreature.getId()) ? ownCreature : opposingCreature;
        harness.handlePermanentChosen(player1, selected.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Strax, Sontaran Nurse");
        harness.assertInGraveyard(selected == ownCreature ? player1 : player2, "Grizzly Bears");
        assertThat(strax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
