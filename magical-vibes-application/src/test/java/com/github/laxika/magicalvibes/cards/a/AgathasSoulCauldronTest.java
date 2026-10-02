package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgathasSoulCauldron.class, DrudgeSkeletons.class, GrizzlyBears.class, RodOfRuin.class})
class AgathasSoulCauldronTest extends BaseCardTest {

    @Test
    void exilesCreatureAndPutsCounterOnTargetCreature() {
        Permanent cauldron = addCreatureReady(player1, new AgathasSoulCauldron());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player1, new ArrayList<>(List.of(skeletons)));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(skeletons.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(cauldron.getId())).containsExactly(skeletons);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getCardsExiledByPermanent(cauldron.getId())).containsExactly(skeletons);
    }

    @Test
    void doesNotPutCounterWhenExiledCardIsNotCreature() {
        Permanent cauldron = addCreatureReady(player1, new AgathasSoulCauldron());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(rod.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getCardsExiledByPermanent(cauldron.getId())).containsExactly(rod);
    }

    @Test
    void counteredCreatureGainsActivatedAbilitiesOfExiledCreatureAndCanUseAnyManaColor() {
        Permanent cauldron = addCreatureReady(player1, new AgathasSoulCauldron());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card skeletons = new DrudgeSkeletons();
        gd.addToExile(player1.getId(), skeletons, cauldron.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void creatureWithoutCounterDoesNotGainExiledCreatureAbilities() {
        Permanent cauldron = addCreatureReady(player1, new AgathasSoulCauldron());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.addToExile(player1.getId(), new DrudgeSkeletons(), cauldron.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canExileOpponentsCreatureWithoutControllingAnyCreatures() {
        Permanent cauldron = addCreatureReady(player1, new AgathasSoulCauldron());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, new ArrayList<>(List.of(skeletons)));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(skeletons.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(cauldron.getId())).containsExactly(skeletons);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void nativeCreatureAbilityCanUseAnyColorWithoutCounters() {
        addCreatureReady(player1, new AgathasSoulCauldron());
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skeletons.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsCounteredCreatureDoesNotGainAbilities() {
        Permanent cauldron = addCreatureReady(player1, new AgathasSoulCauldron());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.addToExile(player1.getId(), new DrudgeSkeletons(), cauldron.getId());
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingLastCounterRemovesGrantedAbility() {
        Permanent cauldron = addCreatureReady(player1, new AgathasSoulCauldron());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.addToExile(player1.getId(), new DrudgeSkeletons(), cauldron.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
