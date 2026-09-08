package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TinybonesThePickpocket.class, GrizzlyBears.class, Shock.class})
class TinybonesThePickpocketTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets a nonland permanent in the damaged player's graveyard")
    void targetsNonlandPermanentInDamagedPlayersGraveyard() {
        Card ownPermanent = new GrizzlyBears();
        Card opponentInstant = new Shock();
        Card opponentPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownPermanent));
        harness.setGraveyard(player2, List.of(opponentInstant, opponentPermanent));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentPermanent.getId());
    }

    @Test
    @DisplayName("Casts the targeted card using mana of any type")
    void castsTargetedCardUsingManaOfAnyType() {
        Card opponentPermanent = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentPermanent));

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(opponentPermanent.getId()));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, opponentPermanent.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer a nonpermanent card")
    void doesNotOfferNonpermanentCard() {
        harness.setGraveyard(player2, List.of(new Shock()));

        attackDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void attackDealingDamage() {
        Permanent tinybones = addCreatureReady(player1, new TinybonesThePickpocket());
        tinybones.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
