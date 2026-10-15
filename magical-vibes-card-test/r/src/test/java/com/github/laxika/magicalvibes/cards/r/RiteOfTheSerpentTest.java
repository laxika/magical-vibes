package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfTheSerpent.class, AlpineGrizzly.class})
class RiteOfTheSerpentTest extends BaseCardTest {

    @Test
    void destroysCreatureWithCounterAndCreatesOneSnakeForSpellController() {
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castRite(target);

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Alpine Grizzly");
        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(countPermanents(player2, "Snake")).isZero();
    }

    @Test
    void destroysCreatureWithoutCounterAndCreatesNoSnake() {
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());
        castRite(target);

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        assertThat(countPermanents(player1, "Snake")).isZero();
        assertThat(countPermanents(player2, "Snake")).isZero();
    }

    @Test
    void createsSnakeWhenCounteredIndestructibleCreatureIsNotDestroyed() {
        Card indestructibleBears = new AlpineGrizzly();
        indestructibleBears.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        Permanent target = addCreatureReady(player2, indestructibleBears);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castRite(target);

        harness.assertOnBattlefield(player2, "Alpine Grizzly");
        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(countPermanents(player2, "Snake")).isZero();
    }

    @Test
    void createsSnakeWhenDestroyingOwnCreatureWithCounter() {
        Permanent target = addCreatureReady(player1, new AlpineGrizzly());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castRite(target);

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(countPermanents(player2, "Snake")).isZero();
    }

    @Test
    void otherCounterTypesDoNotCreateSnake() {
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());
        target.setCounterCount(CounterType.CHARGE, 1);
        castRite(target);

        harness.assertInGraveyard(player2, "Alpine Grizzly");
        assertThat(countPermanents(player1, "Snake")).isZero();
        assertThat(countPermanents(player2, "Snake")).isZero();
    }

    @Test
    void checksCountersAtResolutionRatherThanCasting() {
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareRite();
        harness.castSorcery(player1, 0, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpine Grizzly");
        assertThat(countPermanents(player1, "Snake")).isZero();
        assertThat(countPermanents(player2, "Snake")).isZero();
    }

    @Test
    void illegalTargetPreventsTokenCreation() {
        Permanent target = addCreatureReady(player2, new AlpineGrizzly());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareRite();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Snake")).isZero();
        assertThat(countPermanents(player2, "Snake")).isZero();
        harness.assertInGraveyard(player1, "Rite of the Serpent");
    }

    private void castRite(Permanent target) {
        prepareRite();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void prepareRite() {
        harness.setHand(player1, List.of(new RiteOfTheSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);
    }
}
