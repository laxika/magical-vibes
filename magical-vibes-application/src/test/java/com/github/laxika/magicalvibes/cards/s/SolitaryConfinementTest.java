package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BreakingPoint;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolitaryConfinement.class, BreakingPoint.class, GiantWarthog.class, LavaDart.class})
class SolitaryConfinementTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card to keep Solitary Confinement during upkeep")
    void discardsCardInsteadOfSacrificing() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of(new LavaDart()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Solitary Confinement");
        harness.assertInGraveyard(player1, "Lava Dart");
    }

    @Test
    @DisplayName("Sacrifices itself during upkeep when its controller has no card to discard")
    void sacrificesWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Solitary Confinement");
        harness.assertInGraveyard(player1, "Solitary Confinement");
    }

    @Test
    @DisplayName("Sacrifices itself when its controller declines to discard")
    void sacrificesWhenControllerDeclinesToDiscard() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of(new LavaDart()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Solitary Confinement");
        harness.assertInGraveyard(player1, "Solitary Confinement");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Controller skips their draw step while Solitary Confinement is on the battlefield")
    void skipsControllerDrawStep() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of(new LavaDart()));
        harness.setLibrary(player1, List.of(new LavaDart()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        int handBeforeDrawStep = gd.playerHands.get(player1.getId()).size();
        int libraryBeforeDrawStep = gd.playerDecks.get(player1.getId()).size();
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeDrawStep);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBeforeDrawStep);
    }

    @Test
    @DisplayName("Controller cannot be targeted while Solitary Confinement is on the battlefield")
    void controllerHasShroud() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LavaDart()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Controller cannot target themselves while Solitary Confinement is on the battlefield")
    void controllerCannotTargetThemselfWithShroud() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Prevents noncombat damage to its controller")
    void preventsDamageToController() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new BreakingPoint(), new BreakingPoint()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, true);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Prevents combat damage to its controller")
    void preventsCombatDamageToController() {
        harness.addToBattlefield(player1, new SolitaryConfinement());
        addCreatureReady(player2, new GiantWarthog());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }
}
