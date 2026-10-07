package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.assertOnBattlefield(player1, "Triarch Praetorian");
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

    @Test
    @DisplayName("Dynastic Codes is a single trigger that draws and loses life together")
    void dynasticCodesResolvesAsOneAbility() {
        harness.setGraveyard(player1, List.of(new TriarchPraetorian()));
        harness.setLibrary(player1, List.of(new TriarchPraetorian(), new TriarchPraetorian()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("At two life, Dynastic Codes draws both cards before its controller loses")
    void drawsBeforeLethalLifeLoss() {
        harness.setLife(player1, 2);
        harness.setGraveyard(player1, List.of(new TriarchPraetorian()));
        TriarchPraetorian firstDraw = new TriarchPraetorian();
        TriarchPraetorian secondDraw = new TriarchPraetorian();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Unearth exiles the creature at the next end step")
    void unearthExilesAtEndStep() {
        TriarchPraetorian card = new TriarchPraetorian();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Triarch Praetorian");
        harness.assertNotInGraveyard(player1, "Triarch Praetorian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("A lethally damaged unearthed creature is exiled instead of dying")
    void unearthExilesInsteadOfDying() {
        TriarchPraetorian card = new TriarchPraetorian();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        findPermanent(player1, "Triarch Praetorian").setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Triarch Praetorian");
        harness.assertNotInGraveyard(player1, "Triarch Praetorian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Unearth can only be activated during a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new TriarchPraetorian()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Triarch Praetorian");
    }

    @Test
    @DisplayName("Unearth cannot be activated while another ability is on the stack")
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new TriarchPraetorian(), new TriarchPraetorian()));
        harness.addMana(player1, ManaColor.BLACK, 10);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unearth requires black mana")
    void unearthRequiresBlackMana() {
        harness.setGraveyard(player1, List.of(new TriarchPraetorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Triarch Praetorian");
    }
}
