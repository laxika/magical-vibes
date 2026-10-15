package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.o.OmenportVigilante;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinybonesThePickpocket.class, OmenportVigilante.class, ThunderSalvo.class, Swamp.class})
class TinybonesThePickpocketTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets a nonland permanent in the damaged player's graveyard")
    void targetsNonlandPermanentInDamagedPlayersGraveyard() {
        Card ownPermanent = new OmenportVigilante();
        Card opponentInstant = new ThunderSalvo();
        Card opponentPermanent = new OmenportVigilante();
        harness.setGraveyard(player1, List.of(ownPermanent));
        harness.setGraveyard(player2, List.of(opponentInstant, opponentPermanent));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentPermanent.getId());
    }

    @Test
    @DisplayName("Casts the targeted card during resolution using mana of any type")
    void castsTargetedCardUsingManaOfAnyType() {
        Card opponentPermanent = new OmenportVigilante();
        harness.setGraveyard(player2, List.of(opponentPermanent));

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(opponentPermanent.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Omenport Vigilante");
    }

    @Test
    @DisplayName("Does not offer a nonpermanent card")
    void doesNotOfferNonpermanentCard() {
        harness.setGraveyard(player2, List.of(new ThunderSalvo()));

        attackDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not offer a land card")
    void doesNotOfferLandCard() {
        harness.setGraveyard(player2, List.of(new Swamp()));

        attackDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot wait for the main phase to cast the targeted card")
    void cannotCastTargetLaterInTurn() {
        Card opponentPermanent = new OmenportVigilante();
        harness.setGraveyard(player2, List.of(opponentPermanent));

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(opponentPermanent.getId()));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Omenport Vigilante");
    }

    private void attackDealingDamage() {
        Permanent tinybones = addCreatureReady(player1, new TinybonesThePickpocket());
        tinybones.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
