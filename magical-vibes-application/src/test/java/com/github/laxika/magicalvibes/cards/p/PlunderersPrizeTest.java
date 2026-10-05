package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ContainmentPriest;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlunderersPrize.class, FountainOfYouth.class, GrizzlyBears.class,
        MindStone.class, Ornithopter.class, DarksteelCitadel.class, ContainmentPriest.class})
class PlunderersPrizeTest extends BaseCardTest {

    @Test
    void belowXArtifactReturnsSpellAndPerpetuallyIncreasesItsCost() {
        Card prize = new PlunderersPrize();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        addManaForX(2);

        harness.castAndResolveSorcery(player1, 0, 2);

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(prize);
    }

    @Test
    void artifactLandIsNotSought() {
        Card prize = new PlunderersPrize();
        Card land = new DarksteelCitadel();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(land));
        addManaForX(1);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertNotOnBattlefield(player1, "Darksteel Citadel");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
    }

    @Test
    void artifactAboveXIsNotSought() {
        Card prize = new PlunderersPrize();
        Card stone = new MindStone();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(stone));
        addManaForX(1);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(stone);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
    }

    @Test
    void emptyLibraryDoesNotReturnSpell() {
        Card prize = new PlunderersPrize();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of());
        addManaForX(2);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
        harness.assertNotInHand(player1, "Plunderer's Prize");
    }

    @Test
    void repeatedBelowXResolutionsAccumulateCastCostIncreases() {
        Card prize = new PlunderersPrize();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new FountainOfYouth()));
        addManaForX(1);
        harness.castAndResolveSorcery(player1, 0, 1);

        addManaForX(2);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prize);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        addManaForX(1);
        assertThatThrownBy(() -> harness.castSorceryForX(player1, 0, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
    }

    @Test
    void creatureArtifactCanBeSought() {
        Card prize = new PlunderersPrize();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        addManaForX(1);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prize);
    }

    @Test
    void exiledInsteadOfEnteringDoesNotReturnOrIncreaseSpellCost() {
        Card prize = new PlunderersPrize();
        Card thopter = new Ornithopter();
        harness.addToBattlefield(player2, new ContainmentPriest());
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(thopter));
        addManaForX(1);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(thopter);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
        harness.assertNotInHand(player1, "Plunderer's Prize");
        assertThat(gd.perpetualCardCastCostIncreases).doesNotContainKey(prize.getId());
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
