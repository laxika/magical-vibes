package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelentlessAdvance.class, GrizzlyBears.class, ParallelLives.class})
class RelentlessAdvanceTest extends BaseCardTest {

    @Test
    void amassesThreeWithoutAnArmy() {
        castRelentlessAdvance();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getEffectivePower()).isEqualTo(3);
        assertThat(army.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void amassesThreeOnAnExistingArmyAndMakesItZombie() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castRelentlessAdvance();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void choosesOnlyOneOfMultipleArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        castRelentlessAdvance();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .contains(CardSubtype.BEAR, CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotDeclineChoosingAnArmy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        castRelentlessAdvance();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void ignoresOpponentArmy() {
        Permanent opponentArmy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentArmy.getGrantedSubtypes().add(CardSubtype.ARMY);

        castRelentlessAdvance();

        assertThat(opponentArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(3));
    }

    @Test
    void doubledTokenCreationPutsCountersOnOnlyOneArmy() {
        harness.addToBattlefield(player1, new ParallelLives());

        castRelentlessAdvance();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        List<Permanent> armies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(armies).hasSize(2);
        assertThat(armies).allSatisfy(army ->
                assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        harness.handleMultiplePermanentsChosen(player1, List.of(armies.getFirst().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(army -> {
                    assertThat(army.getId()).isEqualTo(armies.getFirst().getId());
                    assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
                });
    }

    private void castRelentlessAdvance() {
        harness.setHand(player1, List.of(new RelentlessAdvance()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
