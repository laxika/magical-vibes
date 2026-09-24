package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoolResources.class, Forest.class, GrizzlyBears.class})
class PoolResourcesTest extends BaseCardTest {

    @Test
    void withoutGiftDrawsTwoCardsAndCreatesNoFish() {
        GrizzlyBears first = new GrizzlyBears();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new PoolResources()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGift(player1, 0, null, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(findPermanents(player2, "Fish")).isEmpty();
    }

    @Test
    void promisedGiftCreatesTappedFishAndSeeksTwoRandomNonlandCards() {
        GrizzlyBears first = new GrizzlyBears();
        Forest land = new Forest();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(new PoolResources()));
        harness.setLibrary(player1, List.of(first, land, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGift(player1, 0, null, true);
        harness.passBothPriorities();

        Permanent fish = findPermanent(player2, "Fish");
        assertThat(fish).isNotNull();
        assertThat(fish.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }
}
