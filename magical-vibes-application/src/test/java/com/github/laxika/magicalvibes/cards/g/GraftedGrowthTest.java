package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BruteSuit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraftedGrowth.class, Forest.class, JukaiTrainee.class, BruteSuit.class})
class GraftedGrowthTest extends BaseCardTest {

    @Test
    void entersAttachedToLandAndPutsCounterOnControlledCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grafted Growth").getAttachedTo()).isEqualTo(forest.getId());
    }

    @Test
    void entersCanPutCounterOnControlledVehicle() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BruteSuit());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, vehicle.getId());
        resolveAllTriggers();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void enchantedLandCanTapForTwoManaOfChosenColor() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GraftedGrowth());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void canResolveWithoutAnyCreatureOrVehicleToReceiveCounter() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grafted Growth").getAttachedTo()).isEqualTo(forest.getId());
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotEnchantNonlandCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseOpponentsCreatureForCounter() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canEnchantOpponentsLandAndItsControllerGetsMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Grafted Growth").getAttachedTo()).isEqualTo(forest.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void canChooseCreatureThatArrivedAfterAuraWasCast() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterTriggerResolvesAfterAuraLeavesBattlefield() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        Permanent aura = findPermanent(player1, "Grafted Growth");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotChooseCounterTargetWhileCastingAuraSpell() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.setHand(player1, List.of(new GraftedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(forest.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
