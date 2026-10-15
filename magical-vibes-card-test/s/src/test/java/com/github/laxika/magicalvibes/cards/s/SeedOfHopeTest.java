package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedOfHope.class, Forest.class, VolcanicSpite.class})
class SeedOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards, may return a milled permanent, and gains 2 life")
    void millsReturnsPermanentAndGainsLife() {
        harness.setLife(player1, 20);
        setTopCards(new Forest(), new VolcanicSpite());

        castAndResolve();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains 2 life when the optional return is declined")
    void decliningReturnStillGainsLife() {
        harness.setLife(player1, 20);
        setTopCards(new Forest(), new VolcanicSpite());

        castAndResolve();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains 2 life without offering a nonpermanent card")
    void noPermanentMilled() {
        harness.setLife(player1, 20);
        setTopCards(new VolcanicSpite(), new VolcanicSpite());

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Volcanic Spite");
    }

    @Test
    void emptyLibraryStillGainsLife() {
        harness.setLife(player1, 20);
        setTopCards();

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Seed of Hope");
    }

    @Test
    void oneCardLibraryCanReturnItsPermanent() {
        harness.setLife(player1, 20);
        setTopCards(new Forest());

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertLife(player1, 22);
    }

    @Test
    void canDeclineFirstPermanentAndReturnSecondWithoutMillingThirdCard() {
        harness.setLife(player1, 20);
        Forest first = new Forest();
        Forest second = new Forest();
        VolcanicSpite third = new VolcanicSpite();
        setTopCards(first, second, third);

        castAndResolve();
        harness.assertLife(player1, 20);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
    }

    @Test
    void returningFirstPermanentDoesNotOfferSecondOrAnOlderGraveyardCard() {
        harness.setLife(player1, 20);
        Forest older = new Forest();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setGraveyard(player1, List.of(older));
        setTopCards(first, second);

        castAndResolve();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(older, second).doesNotContain(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 22);
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeedOfHope()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0);
    }

    private void setTopCards(com.github.laxika.magicalvibes.model.Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
