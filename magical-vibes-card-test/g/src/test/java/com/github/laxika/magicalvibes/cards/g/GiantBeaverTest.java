package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PatientNaturalist;
import com.github.laxika.magicalvibes.cards.r.RevokePrivileges;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantBeaver.class, PatientNaturalist.class, RevokePrivileges.class})
class GiantBeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 3 taps other creatures and saddles Giant Beaver")
    void saddleTapsOtherCreatures() {
        Permanent beaver = addCreatureReady(player1, new GiantBeaver());
        Permanent firstHelper = addCreatureReady(player1, new PatientNaturalist());
        Permanent secondHelper = addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(beaver.isSaddled()).isTrue();
        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled puts a +1/+1 counter on a creature that saddled it")
    void attacksWhileSaddledCountersSaddler() {
        Permanent beaver = addCreatureReady(player1, new GiantBeaver());
        Permanent saddler = addCreatureReady(player1, new PatientNaturalist());
        addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, saddler.getId());
        harness.passBothPriorities();

        assertThat(saddler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(beaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking while not saddled does not put a counter on a creature")
    void attacksWhileNotSaddledDoesNotCounter() {
        addCreatureReady(player1, new GiantBeaver());
        Permanent creature = addCreatureReady(player1, new PatientNaturalist());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only creatures that saddled this Mount are eligible for the attack counter")
    void attackTargetChoiceExcludesOtherCreatures() {
        Permanent beaver = addCreatureReady(player1, new GiantBeaver());
        Permanent firstSaddler = addCreatureReady(player1, new PatientNaturalist());
        Permanent secondSaddler = addCreatureReady(player1, new PatientNaturalist());
        firstSaddler.setSummoningSick(true);
        secondSaddler.setSummoningSick(true);
        Permanent bystander = addCreatureReady(player1, new PatientNaturalist());
        bystander.tap();
        Permanent opponentCreature = addCreatureReady(player2, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstSaddler.getId(), secondSaddler.getId());
        harness.handlePermanentChosen(player1, secondSaddler.getId());
        resolveAllTriggers();

        assertThat(secondSaddler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(firstSaddler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bystander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(beaver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack counter resolves even if Giant Beaver leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent beaver = addCreatureReady(player1, new GiantBeaver());
        Permanent saddler = addCreatureReady(player1, new PatientNaturalist());
        addCreatureReady(player1, new PatientNaturalist());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, saddler.getId());

        gd.playerBattlefields.get(player1.getId()).remove(beaver);
        gd.playerGraveyards.get(player1.getId()).add(beaver.getCard());
        resolveAllTriggers();

        assertThat(saddler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature that cannot crew Vehicles can still saddle Giant Beaver")
    void cannotCrewRestrictionDoesNotPreventSaddling() {
        Permanent beaver = addCreatureReady(player1, new GiantBeaver());
        Permanent enchantedSaddler = addCreatureReady(player1, new PatientNaturalist());
        Permanent otherSaddler = addCreatureReady(player1, new PatientNaturalist());
        harness.setHand(player1, List.of(new RevokePrivileges()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, enchantedSaddler.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(beaver.isSaddled()).isTrue();
        assertThat(enchantedSaddler.isTapped()).isTrue();
        assertThat(otherSaddler.isTapped()).isTrue();
    }
}
