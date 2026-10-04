package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.ServantOfTheConduit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FabricationModule.class, ServantOfTheConduit.class})
class FabricationModuleTest extends BaseCardTest {

    @Test
    void paysFourManaAndGivesEnergy() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new FabricationModule());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(module.isTapped()).isTrue();
    }

    @Test
    void putsCounterOnTargetCreatureYouControlWhenYouGetEnergy() {
        harness.addToBattlefield(player1, new FabricationModule());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void gainingTwoEnergyAtOnceTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new FabricationModule());
        harness.castFromHand(player1, new ServantOfTheConduit(), "{1}{G}");
        resolveAllTriggers();

        Permanent creature = findPermanent(player1, "Servant of the Conduit");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsEnergyGainDoesNotTriggerModule() {
        harness.addToBattlefield(player1, new FabricationModule());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        harness.addToBattlefield(player2, new FabricationModule());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void triggerOffersOnlyCreaturesYouControl() {
        harness.addToBattlefield(player1, new FabricationModule());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        harness.addToBattlefield(player2, new ServantOfTheConduit());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterTriggerResolvesAfterModuleLeavesBattlefield() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new FabricationModule());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(module);
        gd.playerGraveyards.get(player1.getId()).add(module.getCard());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterTriggerDoesNothingWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new FabricationModule());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
