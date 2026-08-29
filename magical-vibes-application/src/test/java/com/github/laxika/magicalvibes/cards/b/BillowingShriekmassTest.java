package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BillowingShriekmassTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from its controller's library")
    void etbMillsThreeCards() {
        harness.setHand(player1, List.of(new BillowingShriekmass()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 3);
    }

    @Test
    @DisplayName("Threshold gives Billowing Shriekmass +2/+1")
    void thresholdBoostsAtSevenCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent shriekmass = harness.addToBattlefieldAndReturn(player1, new BillowingShriekmass());

        int powerBelowThreshold = gqs.getEffectivePower(gd, shriekmass);
        int toughnessBelowThreshold = gqs.getEffectiveToughness(gd, shriekmass);

        harness.setGraveyard(player1, graveyardCards(7));

        assertThat(gqs.getEffectivePower(gd, shriekmass) - powerBelowThreshold).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shriekmass) - toughnessBelowThreshold).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player1, List.of());
        Permanent shriekmass = harness.addToBattlefieldAndReturn(player1, new BillowingShriekmass());

        int powerWithoutThreshold = gqs.getEffectivePower(gd, shriekmass);
        int toughnessWithoutThreshold = gqs.getEffectiveToughness(gd, shriekmass);

        harness.setGraveyard(player2, graveyardCards(7));

        assertThat(gqs.getEffectivePower(gd, shriekmass)).isEqualTo(powerWithoutThreshold);
        assertThat(gqs.getEffectiveToughness(gd, shriekmass)).isEqualTo(toughnessWithoutThreshold);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
