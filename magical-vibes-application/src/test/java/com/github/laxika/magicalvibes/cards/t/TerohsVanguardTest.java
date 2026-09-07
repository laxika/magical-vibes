package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerohsVanguard.class, GrizzlyBears.class, Shock.class})
class TerohsVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ETB grants your creatures protection from black until end of turn")
    void thresholdEtbGrantsProtectionFromBlack() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(otherCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(opponentCreature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Threshold ETB does not trigger below seven graveyard cards")
    void thresholdEtbDoesNotTriggerBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(otherCreature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Threshold ETB protection wears off at cleanup")
    void protectionWearsOffAtCleanup() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent vanguard = castVanguard();

        vanguard.resetModifiers();
        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    private Permanent castVanguard() {
        harness.setHand(player1, List.of(new TerohsVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent vanguard = findPermanent(player1, "Teroh's Vanguard");
        harness.passBothPriorities();
        return vanguard;
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
