package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReefWorm.class, WrathOfGod.class})
class ReefWormTest extends BaseCardTest {

    @Test
    @DisplayName("Reef Worm's death chain creates Fish, then Whale, then Kraken tokens")
    void deathChainCreatesSuccessiveTokens() {
        harness.addToBattlefield(player1, new ReefWorm());

        destroyAllCreatures();
        harness.passBothPriorities();

        Permanent fish = findPermanent(player1, "Fish");
        assertThat(fish.getCard().getPower()).isEqualTo(3);
        assertThat(fish.getCard().getToughness()).isEqualTo(3);
        assertThat(fish.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(fish.getCard().getSubtypes()).containsExactly(CardSubtype.FISH);

        destroyAllCreatures();
        harness.passBothPriorities();

        Permanent whale = findPermanent(player1, "Whale");
        assertThat(whale.getCard().getPower()).isEqualTo(6);
        assertThat(whale.getCard().getToughness()).isEqualTo(6);
        assertThat(whale.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(whale.getCard().getSubtypes()).containsExactly(CardSubtype.WHALE);

        destroyAllCreatures();
        harness.passBothPriorities();

        Permanent kraken = findPermanent(player1, "Kraken");
        assertThat(kraken.getCard().getPower()).isEqualTo(9);
        assertThat(kraken.getCard().getToughness()).isEqualTo(9);
        assertThat(kraken.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(kraken.getCard().getSubtypes()).containsExactly(CardSubtype.KRAKEN);
    }

    private void destroyAllCreatures() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
