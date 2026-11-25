package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ContainmentPriest;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlunderersPrize.class, FountainOfYouth.class, GrizzlyBears.class,
        MindStone.class, Ornithopter.class, DarksteelCitadel.class, ContainmentPriest.class,
        GrafdiggersCage.class})
class PlunderersPrizeTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact with mana value less than X returns the spell to hand and makes it perpetually cost {1} more")
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
    @DisplayName("An artifact with mana value equal to X neither returns the spell nor increases its cost")
    void artifactWithManaValueEqualToXDoesNotReturnOrIncreaseCost() {
        Card prize = new PlunderersPrize();
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        addManaForX(0);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(prize);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(prize);
        assertThat(gd.perpetualGenericCastCostIncreases).doesNotContainKey(prize.getId());
    }

    @Test
    @DisplayName("Nonartifact cards are not sought")
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
    @DisplayName("An artifact land is not sought")
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
    @DisplayName("An artifact with mana value greater than X is not sought")
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
    @DisplayName("With an empty library the spell doesn't return")
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
    @DisplayName("Repeated returns add up their cast-cost increases")
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
    @DisplayName("An artifact creature can be sought")
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
    @DisplayName("A sought card exiled instead of entering neither returns the spell nor increases its cost")
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
        assertThat(gd.perpetualGenericCastCostIncreases).doesNotContainKey(prize.getId());
    }

    @Test
    @DisplayName("Grafdigger's Cage doesn't stop the sought creature: it enters from the hand the seek put it in")
    void grafdiggersCageDoesNotStopTheSoughtArtifactCreature() {
        Card prize = new PlunderersPrize();
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setHand(player1, List.of(prize));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        addManaForX(1);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prize);
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
