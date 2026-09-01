package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        Permanent cauldron = addReady(player1, new AgathasSoulCauldron());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player1, new ArrayList<>(List.of(skeletons)));

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(skeletons.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getCardsExiledByPermanent(cauldron.getId())).containsExactly(skeletons);
    }

    @Test
    void doesNotPutCounterWhenExiledCardIsNotCreature() {
        addReady(player1, new AgathasSoulCauldron());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Card rod = new RodOfRuin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(rod)));

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(rod.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counteredCreatureGainsActivatedAbilitiesOfExiledCreatureAndCanUseAnyManaColor() {
        Permanent cauldron = addReady(player1, new AgathasSoulCauldron());
        Permanent bears = addReady(player1, new GrizzlyBears());
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
        Permanent cauldron = addReady(player1, new AgathasSoulCauldron());
        Permanent bears = addReady(player1, new GrizzlyBears());
        gd.addToExile(player1.getId(), new DrudgeSkeletons(), cauldron.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
