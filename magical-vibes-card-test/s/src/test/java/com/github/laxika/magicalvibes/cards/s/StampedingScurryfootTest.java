package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StampedingScurryfoot.class})
class StampedingScurryfootTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts a counter on Stampeding Scurryfoot and creates an Elephant")
    void exhaustPutsCounterAndCreatesElephant() {
        Permanent scurryfoot = addCreatureReady(player1, new StampedingScurryfoot());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scurryfoot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(elephant.getCard().isToken()).isTrue();
        assertThat(elephant.getCard().getPower()).isEqualTo(3);
        assertThat(elephant.getCard().getToughness()).isEqualTo(3);
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
    }

    @Test
    @DisplayName("Exhaust can be activated only once for the permanent")
    void exhaustCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new StampedingScurryfoot());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exhaust works while tapped and summoning sick")
    void exhaustWorksWhileTappedAndSummoningSick() {
        Permanent scurryfoot = harness.addToBattlefieldAndReturn(player1, new StampedingScurryfoot());
        scurryfoot.setSummoningSick(true);
        scurryfoot.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scurryfoot.isTapped()).isTrue();
        assertThat(scurryfoot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
    }

    @Test
    @DisplayName("Exhaust cannot be activated twice before resolution")
    void exhaustLimitAppliesBeforeResolution() {
        Permanent scurryfoot = addCreatureReady(player1, new StampedingScurryfoot());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(scurryfoot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
    }

    @Test
    @DisplayName("Separate permanents can each activate exhaust")
    void exhaustLimitIsPerPermanent() {
        Permanent first = addCreatureReady(player1, new StampedingScurryfoot());
        Permanent second = addCreatureReady(player1, new StampedingScurryfoot());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Elephant")).isEqualTo(2);
    }

    @Test
    @DisplayName("Exhaust creates an Elephant even if its source dies before resolution")
    void exhaustCreatesTokenAfterSourceDies() {
        Permanent scurryfoot = addCreatureReady(player1, new StampedingScurryfoot());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        scurryfoot.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Stampeding Scurryfoot");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stampeding Scurryfoot");
        assertThat(countPermanents(player1, "Elephant")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elephant")).isZero();
    }
}
