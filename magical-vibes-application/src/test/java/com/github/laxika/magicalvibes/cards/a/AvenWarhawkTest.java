package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({AvenWarhawk.class, AvenRiftwatcher.class, AvenBrigadier.class, GrizzlyBears.class})
class AvenWarhawkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each Bird or Soldier card in your hand")
    void entersWithCounterForEachBirdOrSoldierCard() {
        harness.setHand(player1, List.of(
                new AvenWarhawk(), new AvenRiftwatcher(), new AvenBrigadier(), new GrizzlyBears()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findWarhawk().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only Bird and Soldier cards in the controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        harness.setHand(player1, List.of(new AvenWarhawk(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new AvenRiftwatcher(), new AvenBrigadier()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findWarhawk().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent findWarhawk() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Aven Warhawk"))
                .findFirst().orElseThrow();
    }
}
