package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BillowingShriekmass.class, Forest.class})
class BillowingShriekmassTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from its controller's library")
    void etbMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castFromHand(player1, new BillowingShriekmass(), "{3}{B}");
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

    @Test
    @DisplayName("ETB mills only available cards from a short library")
    void etbMillsShortLibrary() {
        List<Card> library = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setGraveyard(player1, List.of());

        harness.castFromHand(player1, new BillowingShriekmass(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB mill enables threshold only after its trigger resolves")
    void etbMillEnablesThreshold() {
        harness.setGraveyard(player1, graveyardCards(4));
        harness.setLibrary(player1, graveyardCards(3));
        harness.castFromHand(player1, new BillowingShriekmass(), "{3}{B}");
        harness.passBothPriorities();
        Permanent shriekmass = gd.playerBattlefields.get(player1.getId()).getFirst();
        int powerBeforeMill = gqs.getEffectivePower(gd, shriekmass);
        int toughnessBeforeMill = gqs.getEffectiveToughness(gd, shriekmass);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gqs.getEffectivePower(gd, shriekmass) - powerBeforeMill).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shriekmass) - toughnessBeforeMill).isEqualTo(1);
    }

    @Test
    @DisplayName("Threshold bonus disappears immediately below seven cards")
    void thresholdBonusDisappears() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent shriekmass = harness.addToBattlefieldAndReturn(player1, new BillowingShriekmass());
        int boostedPower = gqs.getEffectivePower(gd, shriekmass);
        int boostedToughness = gqs.getEffectiveToughness(gd, shriekmass);

        harness.setGraveyard(player1, graveyardCards(6));

        assertThat(boostedPower - gqs.getEffectivePower(gd, shriekmass)).isEqualTo(2);
        assertThat(boostedToughness - gqs.getEffectiveToughness(gd, shriekmass)).isEqualTo(1);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
