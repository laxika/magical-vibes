package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RishkarPeemaRenegade.class, Ornithopter.class})
class RishkarPeemaRenegadeTest extends BaseCardTest {

    @Test
    void putsCountersOnUpToTwoTargetCreatures() {
        Permanent first = addCreatureReady(player1, new Ornithopter());
        Permanent second = addCreatureReady(player2, new Ornithopter());
        harness.setHand(player1, List.of(new RishkarPeemaRenegade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canEnterWithoutTargets() {
        harness.setHand(player1, List.of(new RishkarPeemaRenegade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rishkar, Peema Renegade");
    }

    @Test
    void onlyCounterBearingCreaturesCanTapForGreenMana() {
        Permanent bears = addCreatureReady(player1, new Ornithopter());
        Permanent rishkar = addCreatureReady(player1, new RishkarPeemaRenegade());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(bears.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rishkar.isTapped()).isFalse();
    }

    @Test
    void rishkarWithACounterCanTapForGreenMana() {
        Permanent rishkar = addCreatureReady(player1, new RishkarPeemaRenegade());
        rishkar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(rishkar.isTapped()).isTrue();
    }

    @Test
    void canPutACounterOnExactlyOneCreature() {
        Permanent target = addCreatureReady(player1, new Ornithopter());
        Permanent other = addCreatureReady(player1, new Ornithopter());
        harness.setHand(player1, List.of(new RishkarPeemaRenegade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void anyKindOfCounterGrantsTheManaAbilityAndRemovingItRemovesTheAbility() {
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new RishkarPeemaRenegade());
        creature.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        creature.setTapped(false);
        creature.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void doesNotGrantManaAbilityToOpponentsCreatures() {
        addCreatureReady(player1, new RishkarPeemaRenegade());
        Permanent opponentCreature = addCreatureReady(player2, new Ornithopter());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void grantedTapAbilityStillRequiresTheCreatureToBeFreeOfSummoningSickness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        addCreatureReady(player1, new RishkarPeemaRenegade());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void manaAbilityDisappearsWhenRishkarLeavesTheBattlefield() {
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        Permanent rishkar = addCreatureReady(player1, new RishkarPeemaRenegade());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        creature.setTapped(false);
        gd.playerBattlefields.get(player1.getId()).remove(rishkar);
        gd.playerGraveyards.get(player1.getId()).add(rishkar.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }
}
