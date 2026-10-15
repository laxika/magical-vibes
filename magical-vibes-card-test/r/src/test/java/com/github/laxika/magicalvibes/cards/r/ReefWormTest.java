package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @Test
    @DisplayName("Kraken death ends the token chain")
    void krakenDeathEndsTheChain() {
        harness.addToBattlefield(player1, new ReefWorm());

        for (int death = 0; death < 3; death++) {
            destroyAllCreatures();
            harness.passBothPriorities();
        }
        harness.assertOnBattlefield(player1, "Kraken");

        destroyAllCreatures();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's death chain creates tokens for that opponent")
    void opponentControlsEachSuccessiveToken() {
        harness.addToBattlefield(player2, new ReefWorm());

        for (String tokenName : new String[]{"Fish", "Whale", "Kraken"}) {
            destroyAllCreatures();
            assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, tokenName);
            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        }
    }

    private void destroyAllCreatures() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
