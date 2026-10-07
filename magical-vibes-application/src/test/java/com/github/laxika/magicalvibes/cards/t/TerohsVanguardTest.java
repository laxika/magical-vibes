package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SternJudge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerohsVanguard.class, SternJudge.class})
class TerohsVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold ETB grants your creatures protection from black until end of turn")
    void thresholdEtbGrantsProtectionFromBlack() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SternJudge());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SternJudge());

        Permanent vanguard = castVanguard();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new SternJudge());

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(otherCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(opponentCreature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(lateCreature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Threshold ETB does not trigger below seven graveyard cards")
    void thresholdEtbDoesNotTriggerBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new SternJudge());

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(otherCreature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Threshold checks the entering creature's controller's graveyard")
    void thresholdUsesEnteringControllersGraveyard() {
        harness.setGraveyard(player2, graveyardCards(7));

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Threshold ETB trigger resolves after threshold is lost")
    void thresholdEtbTriggerResolvesAfterThresholdIsLost() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.castFromHand(player1, new TerohsVanguard(), "{3}{W}");
        harness.passBothPriorities();
        Permanent vanguard = findPermanent(player1, "Teroh's Vanguard");

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Threshold ETB protection wears off at cleanup")
    void protectionWearsOffAtCleanup() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent vanguard = castVanguard();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Threshold gained while the spell is on the stack grants the entry trigger")
    void thresholdGainedBeforeEntryTriggers() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.castFromHand(player1, new TerohsVanguard(), "{3}{W}");
        harness.setGraveyard(player1, graveyardCards(7));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Teroh's Vanguard")
                .getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Gaining threshold after entry does not retroactively trigger the ability")
    void thresholdGainedAfterEntryDoesNotTrigger() {
        harness.setGraveyard(player1, graveyardCards(6));
        harness.castFromHand(player1, new TerohsVanguard(), "{3}{W}");
        harness.passBothPriorities();
        Permanent vanguard = findPermanent(player1, "Teroh's Vanguard");

        harness.setGraveyard(player1, graveyardCards(7));
        harness.passBothPriorities();

        assertThat(vanguard.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("The trigger protects creatures present at resolution even if Vanguard has left")
    void triggerProtectsCreaturesAtResolutionAfterSourceLeaves() {
        harness.setGraveyard(player1, graveyardCards(7));
        harness.castFromHand(player1, new TerohsVanguard(), "{3}{W}");
        harness.passBothPriorities();
        Permanent vanguard = findPermanent(player1, "Teroh's Vanguard");
        gd.playerBattlefields.get(player1.getId()).remove(vanguard);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new SternJudge());

        harness.passBothPriorities();

        assertThat(recipient.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    private Permanent castVanguard() {
        harness.castFromHand(player1, new TerohsVanguard(), "{3}{W}");
        harness.passBothPriorities();
        Permanent vanguard = findPermanent(player1, "Teroh's Vanguard");
        harness.passBothPriorities();
        return vanguard;
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new SternJudge());
        }
        return cards;
    }
}
