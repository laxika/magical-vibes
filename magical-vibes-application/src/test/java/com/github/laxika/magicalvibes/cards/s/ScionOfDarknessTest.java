package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
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

@CardUsed({ScionOfDarkness.class, GrizzlyBears.class, HolyDay.class})
class ScionOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage reanimates a creature from the damaged player's graveyard")
    void combatDamageReanimatesChosenCreature() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        attackWithScionDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The optional reanimation may be declined")
    void reanimationMayBeDeclined() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        attackWithScionDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Only creature cards from the damaged player's graveyard are offered")
    void onlyDamagedPlayersCreatureCardsAreOffered() {
        Card creature = new GrizzlyBears();
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(creature, instant));

        attackWithScionDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Cycling discards Scion of Darkness and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ScionOfDarkness()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scion of Darkness");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void attackWithScionDealingDamage() {
        Permanent scion = addCreatureReady(player1, new ScionOfDarkness());
        scion.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
