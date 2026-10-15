package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Revenant.class, GrizzlyBears.class, Shock.class})
class RevenantTest extends BaseCardTest {

    @Test
    @DisplayName("Revenant is 0/0 with no creature cards in controller's graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addRevenantReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Revenant P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addRevenantReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Revenant does NOT count creature cards in opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent perm = addRevenantReady(player1);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Revenant only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addRevenantReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Shock());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Revenant does NOT count token creatures in a graveyard")
    void doesNotCountTokenCreatures() {
        Permanent perm = addRevenantReady(player1);
        GrizzlyBears token = new GrizzlyBears();
        token.setToken(true);
        harness.setGraveyard(player1, List.of(token));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Revenant P/T updates when creatures are added to graveyard")
    void ptUpdatesWhenCreaturesAddedToGraveyard() {
        Permanent perm = addRevenantReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Revenant counts the graveyard of its current controller")
    void followsItsCurrentController() {
        Permanent perm = addRevenantReady(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerBattlefields.get(player2.getId()).add(perm);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Revenant dies with an empty graveyard and then counts itself there")
    void diesWithEmptyGraveyardAndCountsItselfAfterDeath() {
        Revenant card = new Revenant();
        harness.addToBattlefield(player1, card);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Revenant");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);
    }

    @Test
    @DisplayName("Revenant shrinks and dies when the last creature card leaves its graveyard")
    void diesWhenLastCreatureCardLeavesGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Permanent perm = addRevenantReady(player1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Shock()));
        assertThat(gqs.getEffectivePower(gd, perm)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, perm)).isZero();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Revenant");
        harness.assertInGraveyard(player1, "Revenant");
    }

    @Test
    @DisplayName("Revenant's defining ability works in hand and counts its owner's graveyard")
    void definingAbilityWorksInHand() {
        Revenant card = new Revenant();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);
    }

    private Permanent addRevenantReady(Player player) {
        return addCreatureReady(player, new Revenant());
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}
