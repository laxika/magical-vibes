package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaTeamLeader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WestCoastExpansion.class, CaptainAmericaTeamLeader.class, GrizzlyBears.class})
class WestCoastExpansionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws X cards")
    void drawsXCards() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WestCoastExpansion()));
        addManaForX(3);
        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not offer a Hero when X is less than five")
    void doesNotOfferHeroBelowFive() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WestCoastExpansion(), new CaptainAmericaTeamLeader()));
        addManaForX(4);
        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("At X five, may cast a Hero from hand without paying its mana cost")
    void castsHeroAtFiveForFree() {
        CaptainAmericaTeamLeader hero = new CaptainAmericaTeamLeader();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WestCoastExpansion(), hero, bears));
        addManaForX(5);
        harness.castAndResolveSorcery(player1, 0, 5);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(hero.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(hero.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    void zeroDrawsNoCardsAndDoesNotOfferHero() {
        harness.setLibrary(player1, List.of(new WestCoastExpansion()));
        harness.setHand(player1, List.of(new WestCoastExpansion(), new CaptainAmericaTeamLeader()));
        addManaForX(0);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "West Coast Expansion");
    }

    @Test
    void mayDeclineHeroAndKeepAllDrawnCards() {
        harness.setLibrary(player1, List.of(
                new WestCoastExpansion(), new WestCoastExpansion(), new WestCoastExpansion(),
                new WestCoastExpansion(), new WestCoastExpansion()));
        harness.setHand(player1, List.of(new WestCoastExpansion(), new CaptainAmericaTeamLeader()));
        addManaForX(5);
        harness.castAndResolveSorcery(player1, 0, 5);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.assertInHand(player1, "Captain America, Team Leader");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "West Coast Expansion");
    }

    @Test
    void canCastHeroDrawnByTheSpellWhenXIsAboveFive() {
        CaptainAmericaTeamLeader hero = new CaptainAmericaTeamLeader();
        harness.setLibrary(player1, List.of(
                hero, new WestCoastExpansion(), new WestCoastExpansion(),
                new WestCoastExpansion(), new WestCoastExpansion(), new WestCoastExpansion()));
        harness.setHand(player1, List.of(new WestCoastExpansion()));
        addManaForX(6);
        harness.castAndResolveSorcery(player1, 0, 6);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Captain America, Team Leader");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canDeclineFirstHeroAndCastOnlyTheSecond() {
        CaptainAmericaTeamLeader first = new CaptainAmericaTeamLeader();
        CaptainAmericaTeamLeader second = new CaptainAmericaTeamLeader();
        CaptainAmericaTeamLeader third = new CaptainAmericaTeamLeader();
        harness.setLibrary(player1, List.of(
                new WestCoastExpansion(), new WestCoastExpansion(), new WestCoastExpansion(),
                new WestCoastExpansion(), new WestCoastExpansion()));
        harness.setHand(player1, List.of(new WestCoastExpansion(), first, second, third));
        addManaForX(5);
        harness.castAndResolveSorcery(player1, 0, 5);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent ->
                        assertThat(permanent.getCard().getId()).isEqualTo(second.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(first, third).doesNotContain(second).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noHeroInHandDoesNotOfferNonHeroSpellOrOpponentsHero() {
        harness.setLibrary(player1, List.of(
                new WestCoastExpansion(), new WestCoastExpansion(), new WestCoastExpansion(),
                new WestCoastExpansion(), new WestCoastExpansion()));
        harness.setHand(player1, List.of(new WestCoastExpansion()));
        harness.setHand(player2, List.of(new CaptainAmericaTeamLeader()));
        addManaForX(5);

        harness.castAndResolveSorcery(player1, 0, 5);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        harness.assertInHand(player2, "Captain America, Team Leader");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addManaForX(int x) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, x);
    }
}
