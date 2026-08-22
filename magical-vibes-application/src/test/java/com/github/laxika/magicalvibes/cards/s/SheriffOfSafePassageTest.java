package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SheriffOfSafePassage.class, GrizzlyBears.class})
class SheriffOfSafePassageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter when you control no other creatures")
    void entersWithOneCounter() {
        castSheriff();

        Permanent sheriff = findSheriff(player1);
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sheriff.getEffectivePower()).isEqualTo(1);
        assertThat(sheriff.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with an additional counter for each other creature you control")
    void countsOtherControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castSheriff();

        Permanent sheriff = findSheriff(player1);
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(sheriff.getEffectivePower()).isEqualTo(3);
        assertThat(sheriff.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count creatures controlled by an opponent")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSheriff();

        Permanent sheriff = findSheriff(player1);
        assertThat(sheriff.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castSheriff() {
        harness.setHand(player1, List.of(new SheriffOfSafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findSheriff(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SheriffOfSafePassage)
                .findFirst()
                .orElseThrow();
    }
}
