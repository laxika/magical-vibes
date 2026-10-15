package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArborealGrazer;
import com.github.laxika.magicalvibes.cards.q.Quasiduplicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VizierOfTheScorpion.class, ArborealGrazer.class, Quasiduplicate.class})
class VizierOfTheScorpionTest extends BaseCardTest {

    @Test
    void amassesWithoutAnArmyAndGivesTheZombieTokenDeathtouch() {
        castVizierOfTheScorpion();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getEffectivePower()).isEqualTo(1);
        assertThat(army.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, army, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void amassesOnAnExistingArmyAndDoesNotGiveDeathtouchToANontoken() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new ArborealGrazer());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castVizierOfTheScorpion();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, army, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void mustChooseAnArmyWhenMultipleArmiesAreControlled() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VizierOfTheScorpion());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new VizierOfTheScorpion());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);

        castVizierOfTheScorpion();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void opponentsArmyDoesNotPreventCreatingAnArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new VizierOfTheScorpion());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);

        castVizierOfTheScorpion();

        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(army -> {
                    assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
                    assertThat(gqs.hasKeyword(gd, army, Keyword.DEATHTOUCH)).isTrue();
                });
    }

    @Test
    void tokenCopyGrantsItselfDeathtouchWithoutAnotherVizier() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new VizierOfTheScorpion());
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Vizier of the Scorpion"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(original);

        assertThat(gqs.hasKeyword(gd, copy, Keyword.DEATHTOUCH)).isTrue();
    }

    private void castVizierOfTheScorpion() {
        harness.castFromHand(player1, new VizierOfTheScorpion(), "{2}{B}");

        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
