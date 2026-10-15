package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordOfExtinction.class, GrizzlyBears.class, Plains.class, MindRot.class})
class LordOfExtinctionTest extends BaseCardTest {

    @Test
    @DisplayName("Lord of Extinction is 0/0 with no cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addCreatureReady(player1, new LordOfExtinction());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Lord of Extinction P/T counts cards of every type in a graveyard")
    void ptCountsEveryCardType() {
        Permanent perm = addCreatureReady(player1, new LordOfExtinction());

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears()); // creature
        graveyard.add(new Plains());       // land
        graveyard.add(new MindRot());      // sorcery
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lord of Extinction P/T counts cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addCreatureReady(player1, new LordOfExtinction());
        harness.setGraveyard(player1, createCards(2));
        harness.setGraveyard(player2, createCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Lord of Extinction P/T updates as cards enter graveyards")
    void ptUpdatesWhenGraveyardChanges() {
        Permanent perm = addCreatureReady(player1, new LordOfExtinction());
        harness.setGraveyard(player1, createCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lord of Extinction P/T stacks with temporary modifiers")
    void ptStacksWithTemporaryModifiers() {
        Permanent perm = addCreatureReady(player1, new LordOfExtinction());
        harness.setGraveyard(player1, createCards(4));

        perm.setPowerModifier(2);
        perm.setToughnessModifier(2);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Lord of Extinction shrinks when cards leave either graveyard")
    void ptDecreasesWhenCardsLeaveGraveyards() {
        Permanent perm = addCreatureReady(player1, new LordOfExtinction());
        harness.setGraveyard(player1, createCards(2));
        harness.setGraveyard(player2, createCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);

        harness.setGraveyard(player2, List.of());
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Lord of Extinction");
        harness.assertInGraveyard(player1, "Lord of Extinction");
    }

    @Test
    @DisplayName("Lord of Extinction counts itself while in a graveyard")
    void characteristicAbilityWorksInGraveyard() {
        Card lord = new LordOfExtinction();
        harness.setGraveyard(player1, List.of(lord, new Plains()));
        harness.setGraveyard(player2, createCards(2));

        assertThat(gqs.getEffectiveCardPower(gd, lord)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardToughness(gd, lord)).isEqualTo(4);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectiveCardPower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, lord)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lord of Extinction dies after resolving with empty graveyards")
    void diesWhenCastWithEmptyGraveyards() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new LordOfExtinction(), "{3}{B}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lord of Extinction");
        harness.assertInGraveyard(player1, "Lord of Extinction");
    }

    private List<Card> createCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
