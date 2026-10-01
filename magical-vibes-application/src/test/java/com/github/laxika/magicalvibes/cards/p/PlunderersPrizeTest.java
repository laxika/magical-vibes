package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlunderersPrize.class, FountainOfYouth.class, GrizzlyBears.class})
class PlunderersPrizeTest extends BaseCardTest {

    @Test
    void belowXArtifactReturnsSpellAndPerpetuallyIncreasesItsCost() {
        Card prize = new PlunderersPrize();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        addManaForX(2);

        harness.castSorceryForX(player1, 0, 2, Map.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prize);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(prize);

        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castSorceryForX(player1, 0, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactWithManaValueEqualToXDoesNotReturnOrIncreaseCost() {
        Card prize = new PlunderersPrize();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        addManaForX(0);

        harness.castSorceryForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(prize);
        assertThat(gd.perpetualCardCastCostIncreases).doesNotContainKey(prize.getId());
    }

    @Test
    void nonArtifactCardsAreNotSought() {
        Card prize = new PlunderersPrize();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(bears));
        addManaForX(2);

        harness.castSorceryForX(player1, 0, 2, Map.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(prize);
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
