package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowOldTogether.class, Forest.class, GrizzlyBears.class})
class GrowOldTogetherTest extends BaseCardTest {

    @Test
    void seeksUpToTwoCreaturesFromTopTenAndPerpetuallyBoostsCreatureCardsInHand() {
        GrizzlyBears handBear = new GrizzlyBears();
        GrizzlyBears firstSoughtBear = new GrizzlyBears();
        GrizzlyBears secondSoughtBear = new GrizzlyBears();
        GrizzlyBears belowTopTenBear = new GrizzlyBears();
        List<Card> library = new ArrayList<>(List.of(firstSoughtBear));
        for (int i = 0; i < 8; i++) {
            library.add(new Forest());
        }
        library.add(secondSoughtBear);
        library.add(belowTopTenBear);

        harness.setHand(player1, List.of(new GrowOldTogether(), handBear));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(handBear, firstSoughtBear, secondSoughtBear)
                .doesNotContain(belowTopTenBear);
        assertThat(gd.playerDecks.get(player1.getId()))
                .contains(belowTopTenBear)
                .hasSize(9);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> creatures = gd.playerBattlefields.get(player1.getId());
        assertThat(creatures).hasSize(2);
        assertThat(creatures).allSatisfy(permanent -> {
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
        });
    }
}
