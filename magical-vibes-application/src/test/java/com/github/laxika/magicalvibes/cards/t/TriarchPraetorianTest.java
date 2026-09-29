package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TriarchPraetorian.class)
class TriarchPraetorianTest extends BaseCardTest {

    @Test
    @DisplayName("Dynastic Codes does not trigger when cast from hand")
    void castFromHandDoesNotTriggerDynasticCodes() {
        harness.setHand(player1, List.of(new TriarchPraetorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Triarch Praetorian")).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore - 1);
    }

    @Test
    @DisplayName("Dynastic Codes draws two cards and loses 2 life when returned from a graveyard")
    void graveyardEntryDrawsAndLosesLife() {
        harness.setGraveyard(player1, List.of(new TriarchPraetorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Triarch Praetorian").getGrantedKeywords())
                .contains(Keyword.HASTE);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
    }
}
