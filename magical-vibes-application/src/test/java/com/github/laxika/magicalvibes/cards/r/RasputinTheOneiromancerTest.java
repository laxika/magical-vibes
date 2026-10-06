package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed(RasputinTheOneiromancer.class)
class RasputinTheOneiromancerTest extends BaseCardTest {

    @Test
    void entersWithOneDreamCounterAndGivesEachOpponentAGoblin() {
        harness.castFromHand(player1, new RasputinTheOneiromancer(), "{1}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rasputin = findPermanent(player1, "Rasputin, the Oneiromancer");
        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);

        List<Permanent> goblins = findPermanents(player2, "Goblin");
        assertThat(goblins).hasSize(1);
        assertThat(goblins.getFirst().getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(goblins.getFirst().getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    void removesAnyNumberOfDreamCountersForThatMuchColorlessMana() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 3);

        harness.activateAbility(player1, 0, 0, 2, null);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void removesADreamCounterToCreateAProtectedKnight() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
        assertThat(knight.getEffectivePower()).isEqualTo(2);
        assertThat(knight.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.RED)).isTrue();
    }

    @Test
    void dreamCountersArePlacedOnlyWhenTheEnterTriggerResolves() {
        harness.castFromHand(player1, new RasputinTheOneiromancer(), "{1}{W}{U}");
        harness.passBothPriorities();

        Permanent rasputin = findPermanent(player1, "Rasputin, the Oneiromancer");
        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
        assertThat(findPermanents(player2, "Goblin")).hasSize(1);
    }

    @Test
    void canRemoveAllDreamCountersAndManaAbilityDoesNotUseTheStack() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 3);

        harness.activateAbility(player1, 0, 0, 3, null);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
        assertThat(rasputin.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseZeroDreamCountersForMana() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
        assertThat(rasputin.isTapped()).isFalse();
    }

    @Test
    void cannotRemoveMoreDreamCountersThanAvailable() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
        assertThat(rasputin.isTapped()).isFalse();
    }

    @Test
    void knightAbilityPaysItsCostsBeforeResolving() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(rasputin.isTapped()).isTrue();
        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isZero();
        assertThat(findPermanents(player1, "Knight")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLUE)).isFalse();
        assertThat(findPermanents(player2, "Knight")).isEmpty();
    }

    @Test
    void cannotCreateAKnightWithoutADreamCounter() {
        Permanent rasputin = addReadyRasputin();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rasputin.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Knight")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherTapAbilityCanBeUsedWithSummoningSickness() {
        Permanent rasputin = harness.addToBattlefieldAndReturn(player1, new RasputinTheOneiromancer());
        rasputin.setCounterCount(CounterType.DREAM, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
        assertThat(rasputin.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherTapAbilityCanBeUsedWhileTapped() {
        Permanent rasputin = addReadyRasputin();
        rasputin.setCounterCount(CounterType.DREAM, 1);
        rasputin.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(rasputin.getCounterCount(CounterType.DREAM)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyRasputin() {
        return addCreatureReady(player1, new RasputinTheOneiromancer());
    }
}
