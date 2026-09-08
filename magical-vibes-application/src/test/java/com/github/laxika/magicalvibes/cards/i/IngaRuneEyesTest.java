package com.github.laxika.magicalvibes.cards.i;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IngaRuneEyesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a scry 3 trigger")
    void entersWithScryThree() {
        Card top = new Spellbook();
        Card middle = new GrizzlyBears();
        Card bottom = new Spellbook();
        Card next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, middle, bottom, next));
        harness.setHand(player1, List.of(new IngaRuneEyes()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top, middle, bottom);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(middle, next, top, bottom);
    }

    @Test
    @DisplayName("Draws three cards when three creatures die before the trigger resolves")
    void deathTriggerCountsCreaturesDyingWhileItWaits() {
        harness.addToBattlefield(player1, new IngaRuneEyes());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        UUID ingaId = harness.getPermanentId(player1, "Inga Rune-Eyes");

        harness.castInstant(player1, 0, ingaId);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 2 + 3);
    }

    @Test
    @DisplayName("Does not draw when fewer than three creatures died this turn")
    void deathTriggerRequiresThreeCreatures() {
        harness.addToBattlefield(player1, new IngaRuneEyes());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }
}
