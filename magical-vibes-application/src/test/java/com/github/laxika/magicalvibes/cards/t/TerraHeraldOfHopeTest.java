package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerraHeraldOfHope.class, GrizzlyBears.class, ThunderingGiant.class})
class TerraHeraldOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat mills two cards and grants flying until end of turn")
    void beginningOfCombatMillsAndGrantsFlying() {
        Card first = new GrizzlyBears();
        Card second = new ThunderingGiant();
        harness.setLibrary(player1, List.of(first, second));
        Permanent terra = addTerra(player1);

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gqs.hasKeyword(gd, terra, Keyword.FLYING)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, terra, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Combat damage can pay two mana to return a creature with power three or less tapped")
    void combatDamagePaysToReturnSmallCreatureTapped() {
        Card returnedCard = new GrizzlyBears();
        Card tooPowerful = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(returnedCard, tooPowerful));

        dealCombatDamageWithTerra();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returnedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanentByCardId(returnedCard);
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returnedCard);
    }

    @Test
    @DisplayName("Declining the combat-damage payment leaves the graveyard card there")
    void decliningPaymentLeavesCardInGraveyard() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));

        dealCombatDamageWithTerra();
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returnedCard);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    private Permanent addTerra(Player player) {
        return addCreatureReady(player, new TerraHeraldOfHope());
    }

    private void dealCombatDamageWithTerra() {
        Permanent terra = addTerra(player1);
        terra.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanentByCardId(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
