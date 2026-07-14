package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NucklaveeTest extends BaseCardTest {

    /** Casts Nucklavee and resolves it onto the battlefield; the two ETB may triggers sit on the stack. */
    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Nucklavee()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → two ETB may triggers on the stack
    }

    @Test
    @DisplayName("Resolving Nucklavee puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        castAndResolve();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nucklavee"));
    }

    @Test
    @DisplayName("Both triggers return a red sorcery and a blue instant to hand")
    void returnsRedSorceryAndBlueInstant() {
        harness.setGraveyard(player1, List.of(new LavaAxe(), new Opt())); // index 0 red sorcery, 1 blue instant
        castAndResolve();

        // Blue-instant trigger resolves first (LIFO)
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1); // Opt

        // Red-sorcery trigger resolves next; graveyard is now [LavaAxe]
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0); // Lava Axe

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Opt"))
                .anyMatch(c -> c.getName().equals("Lava Axe"));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining both triggers leaves the cards in the graveyard")
    void decliningLeavesCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new LavaAxe(), new Opt()));
        castAndResolve();

        harness.passBothPriorities(); // blue trigger
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities(); // red trigger
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lava Axe"))
                .anyMatch(c -> c.getName().equals("Opt"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Red-sorcery trigger cannot return a red instant")
    void redTriggerRejectsRedInstant() {
        // Shock (red instant) must not be a legal target for either trigger; only Lava Axe (red sorcery) is.
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        castAndResolve();

        // Blue-instant trigger: no blue instant present, accepting is a no-op (no graveyard choice)
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();

        // Red-sorcery trigger: only Lava Axe is a legal choice; Shock (index 0) is rejected
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0)) // Shock
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Blue-instant trigger cannot return a blue sorcery")
    void blueTriggerRejectsBlueSorcery() {
        // Ponder (blue sorcery) must not be a legal target for the blue-instant trigger.
        harness.setGraveyard(player1, List.of(new Ponder()));
        castAndResolve();

        harness.passBothPriorities(); // blue-instant trigger
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ponder"));
    }
}
