package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.y.YavimayaIconoclast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcademyLoremaster.class, YavimayaIconoclast.class})
class AcademyLoremasterTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("The draw-step player may draw an extra card and their spells cost {2} more this turn")
    void acceptingExtraDrawTaxesDrawStepPlayersSpells() {
        harness.addToBattlefield(player1, new AcademyLoremaster());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);

        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new YavimayaIconoclast()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Declining the extra draw does not tax spells")
    void decliningExtraDrawDoesNotTaxSpells() {
        harness.addToBattlefield(player1, new AcademyLoremaster());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);

        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new YavimayaIconoclast()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller is also taxed when they accept their own extra draw")
    void acceptingOwnExtraDrawTaxesControllerSpells() {
        harness.addToBattlefield(player1, new AcademyLoremaster());

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new YavimayaIconoclast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Accepting the draw requires two additional mana for every spell")
    void acceptedDrawRequiresAdditionalManaForEachSpell() {
        harness.addToBattlefield(player1, new AcademyLoremaster());
        advanceToDraw(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new YavimayaIconoclast(), new YavimayaIconoclast()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Accepting two Loremaster draws adds both spell taxes")
    void multipleAcceptedDrawsStackTheirTaxes() {
        harness.addToBattlefield(player1, new AcademyLoremaster());
        harness.addToBattlefield(player2, new AcademyLoremaster());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        advanceToDraw(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);

        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new YavimayaIconoclast()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The spell tax expires at the end of the turn")
    void taxExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new AcademyLoremaster());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        advanceToDraw(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.DRAW);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.DRAW);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new YavimayaIconoclast()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }
}
