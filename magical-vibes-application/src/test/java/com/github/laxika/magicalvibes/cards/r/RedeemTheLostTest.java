package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BoldwyrHeavyweights;
import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedeemTheLost.class, BurrentonBombardier.class, MothdustChangeling.class,
        BoldwyrHeavyweights.class})
class RedeemTheLostTest extends BaseCardTest {

    private void castAtOwnCreature() {
        Permanent bombardier = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        harness.setHand(player1, List.of(new RedeemTheLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bombardier.getId());
        keepClashCardsOnTop();
        harness.handleListChoice(player1, "RED");
        keepClashCardsOnTop();
    }

    private void keepClashCardsOnTop() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.Scry scry) {
            var player = scry.playerId().equals(player1.getId()) ? player1 : player2;
            gs.handleInteractionAnswer(gd, player,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
    }

    @Test
    @DisplayName("Target creature you control gains protection from the chosen color until end of turn")
    void grantsProtectionFromChosenColor() {
        // Equal mana values (Mothdust Changeling, MV 1) mean that nobody wins the clash, so
        // protection is the only relevant effect here.
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));

        castAtOwnCreature();

        Permanent bombardier = findPermanent(player1, "Burrenton Bombardier");
        assertThat(bombardier.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Winning the clash returns Redeem the Lost to its owner's hand")
    void wonClashReturnsSpellToHand() {
        // Higher mana value on top for player1 (Boldwyr Heavyweights, MV 4 > Mothdust Changeling,
        // MV 1) means player1 wins.
        harness.setLibrary(player1, List.of(new BoldwyrHeavyweights()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));

        castAtOwnCreature();

        Permanent bombardier = findPermanent(player1, "Burrenton Bombardier");
        assertThat(bombardier.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        harness.assertInHand(player1, "Redeem the Lost");
        harness.assertNotInGraveyard(player1, "Redeem the Lost");
    }

    @Test
    @DisplayName("Losing the clash sends Redeem the Lost to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        // Lower mana value on top for player1 (Mothdust Changeling, MV 1 < Boldwyr Heavyweights,
        // MV 4) means player1 loses.
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new BoldwyrHeavyweights()));

        castAtOwnCreature();

        harness.assertInGraveyard(player1, "Redeem the Lost");
        harness.assertNotInHand(player1, "Redeem the Lost");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentsCreature() {
        Permanent opponentBombardier = harness.addToBattlefieldAndReturn(player2, new BurrentonBombardier());
        harness.setHand(player1, List.of(new RedeemTheLost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBombardier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection is chosen and granted before the clash reveals library cards")
    void protectionPrecedesClash() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        Card ownTop = new BoldwyrHeavyweights();
        Card opposingTop = new MothdustChangeling();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingTop));
        harness.setHand(player1, List.of(new RedeemTheLost()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
        harness.handleListChoice(player1, "WHITE");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        keepClashCardsOnTop();
        harness.assertInHand(player1, "Redeem the Lost");
        harness.assertNotInGraveyard(player1, "Redeem the Lost");
    }

    @Test
    @DisplayName("A tied clash does not return the spell")
    void tiedClashSendsSpellToGraveyard() {
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));

        castAtOwnCreature();

        harness.assertInGraveyard(player1, "Redeem the Lost");
        harness.assertNotInHand(player1, "Redeem the Lost");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Protection expires at the end of the turn")
    void protectionExpires() {
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));
        castAtOwnCreature();
        Permanent target = findPermanent(player1, "Burrenton Bombardier");

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("An absent target prevents both protection and the clash")
    void missingTargetPreventsClash() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurrentonBombardier());
        Card ownTop = new BoldwyrHeavyweights();
        Card opposingTop = new MothdustChangeling();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opposingTop));
        harness.setHand(player1, List.of(new RedeemTheLost()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        harness.assertInGraveyard(player1, "Redeem the Lost");
        harness.assertNotInHand(player1, "Redeem the Lost");
    }

    @Test
    @DisplayName("Empty libraries still allow protection but nobody wins the clash")
    void emptyLibrariesStillGrantProtection() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        castAtOwnCreature();

        Permanent target = findPermanent(player1, "Burrenton Bombardier");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Redeem the Lost");
        harness.assertNotInHand(player1, "Redeem the Lost");
    }
}
