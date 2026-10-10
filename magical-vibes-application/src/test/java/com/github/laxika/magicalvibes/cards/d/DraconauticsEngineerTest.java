package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconauticsEngineer.class})
@DisplayName("Draconautics Engineer")
class DraconauticsEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("The red Exhaust ability gives other creatures haste and puts a counter on the Engineer")
    void redExhaustAbility() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        Permanent otherEngineer = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, otherEngineer, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, otherEngineer, Keyword.HASTE)).isFalse();
        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The four-mana Exhaust ability creates a flying Dinosaur Dragon")
    void fourManaExhaustAbility() {
        harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dinosaur Dragon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR, CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Each Exhaust ability can be activated only once")
    void eachExhaustAbilityCanBeActivatedOnlyOnce() {
        harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Haste applies only to other creatures controlled at resolution")
    void hasteRecipientsAreFixedAtResolution() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DraconauticsEngineer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());

        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(beforeResolution.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The red exhaust ability works without other creatures")
    void redExhaustWithoutOtherCreatures() {
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(engineer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Exhaust is spent at activation and tracked separately for each permanent")
    void exhaustIsSpentBeforeResolutionAndIndependentBetweenPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DraconauticsEngineer());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }
}
