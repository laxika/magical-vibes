package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LazotepReaver.class, Snarespinner.class, ParallelLives.class})
class LazotepReaverTest extends BaseCardTest {

    @Test
    void amassesWithoutAnArmy() {
        castLazotepReaver();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getEffectivePower()).isEqualTo(1);
        assertThat(army.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void amassesOnAnExistingArmyAndMakesItZombie() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castLazotepReaver();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void choosesOnlyOneOfMultipleArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        castLazotepReaver();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    void opponentsArmyDoesNotPreventCreatingOwnArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new Snarespinner());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);

        castLazotepReaver();

        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(army ->
                        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    void doubledTokenCreationStillPutsCounterOnOnlyOneArmy() {
        harness.addToBattlefield(player1, new ParallelLives());

        castLazotepReaver();

        List<Permanent> armies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(armies).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        Permanent chosen = armies.getFirst();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .containsExactly(chosen);
    }

    private void castLazotepReaver() {
        harness.setHand(player1, List.of(new LazotepReaver()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
