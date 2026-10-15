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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returnedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returnedCard);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Trance still grants flying when there is only one card to mill")
    void shortLibraryStillGrantsFlying() {
        Card milled = new TerraHeraldOfHope();
        harness.setLibrary(player1, List.of(milled));
        Permanent terra = addTerra(player1);

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gqs.hasKeyword(gd, terra, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Trance does not trigger during the opponent's combat")
    void opponentsCombatDoesNotTriggerTrance() {
        Card top = new TerraHeraldOfHope();
        harness.setLibrary(player1, List.of(top));
        Permanent terra = addTerra(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, terra, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Payment can be made even when the graveyard has no legal target")
    void canPayWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        dealCombatDamageWithTerra();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Terra, Herald of Hope")).hasSize(1);
    }

    @Test
    @DisplayName("The reflexive trigger includes power three and excludes the opponent's graveyard")
    void powerThreeInOwnGraveyardIsLegal() {
        Card returnedCard = new TerraHeraldOfHope();
        Card opponentCard = new TerraHeraldOfHope();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        dealCombatDamageWithTerra();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returnedCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returnedCard);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returnedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        Permanent returned = findPermanent(player1, "Terra, Herald of Hope");
        assertThat(returned.getCard().getId()).isEqualTo(returnedCard.getId());
        assertThat(returned.isTapped()).isTrue();
    }

    private Permanent addTerra(Player player) {
        return addCreatureReady(player, new TerraHeraldOfHope());
    }

    private void dealCombatDamageWithTerra() {
        Permanent terra = addTerra(player1);
        terra.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
