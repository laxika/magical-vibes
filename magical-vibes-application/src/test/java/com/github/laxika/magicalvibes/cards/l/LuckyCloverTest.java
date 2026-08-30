package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuckyClover.class, LovestruckBeast.class, HeartsDesire.class})
class LuckyCloverTest extends BaseCardTest {

    @Test
    void copiesAdventureSpell() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())
                .filter(Card::isToken)
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    void doesNotCopyNormalCreatureCast() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }
}
