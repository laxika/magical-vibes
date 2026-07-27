package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhituChroniclerTest extends BaseCardTest {

    // ===== Cast without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 1/3, no ETB trigger")
    void castWithoutKickerNoTrigger() {
        harness.setHand(player1, List.of(new GhituChronicler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Ghitu Chronicler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker — no graveyard interaction even with instant in graveyard")
    void castWithoutKickerIgnoresGraveyard() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new GhituChronicler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Bolt");
    }

    // ===== Cast with kicker =====

    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        castKicked();

        harness.assertOnBattlefield(player1, "Ghitu Chronicler");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — returns instant from graveyard to hand")
    void castWithKickerReturnsInstant() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
    }

    @Test
    @DisplayName("Cast with kicker — returns sorcery from graveyard to hand")
    void castWithKickerReturnsSorcery() {
        harness.setGraveyard(player1, List.of(new Divination()));
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Cast with kicker — does not offer non-instant/sorcery cards from graveyard")
    void castWithKickerDoesNotOfferCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cast with kicker — empty graveyard, no graveyard choice")
    void castWithKickerEmptyGraveyard() {
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    // ===== Helpers =====

    private void castKicked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GhituChronicler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
    }
}
