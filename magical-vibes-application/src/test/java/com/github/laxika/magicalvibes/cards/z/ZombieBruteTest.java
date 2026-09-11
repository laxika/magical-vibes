package com.github.laxika.magicalvibes.cards.z;

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

@CardUsed({ZombieBrute.class, ZombieGoliath.class, GrizzlyBears.class})
class ZombieBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each Zombie card in your hand")
    void entersWithCounterForEachZombieCard() {
        ZombieBrute card = new ZombieBrute();
        harness.setHand(player1, List.of(
                card, new ZombieGoliath(), new ZombieGoliath(), new GrizzlyBears()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanentForCard(card).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only Zombie cards in its controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        ZombieBrute card = new ZombieBrute();
        harness.setHand(player1, List.of(card, new GrizzlyBears()));
        harness.setHand(player2, List.of(new ZombieGoliath(), new ZombieGoliath()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanentForCard(card).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private Permanent findPermanentForCard(ZombieBrute card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
