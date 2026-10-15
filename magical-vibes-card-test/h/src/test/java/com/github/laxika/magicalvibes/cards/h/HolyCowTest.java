package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HolyCow.class, Forest.class, TrainedArynx.class})
class HolyCowTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 2 life before scrying 1")
    void etbGainsLifeAndScries() {
        Card top = new Forest();
        Card bottom = new HolyCow();
        harness.setHand(player1, List.of(new HolyCow()));
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Scry can keep the top card without changing the library order")
    void scryCanKeepTopCard() {
        Card top = new Forest();
        Card bottom = new HolyCow();
        harness.setHand(player1, List.of(new HolyCow()));
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB still gains life with an empty library and needs no scry input")
    void emptyLibraryStillGainsLife() {
        harness.setHand(player1, List.of(new HolyCow()));
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Holy Cow");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's upkeep; only its controller gains life and scries")
    void flashOnOpponentsTurn() {
        Card top = new Forest();
        Card opponentTop = new HolyCow();
        harness.setHand(player1, List.of(new HolyCow()));
        harness.setLibrary(player1, List.of(top));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Holy Cow");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Holy Cow")
    void flyingPreventsGroundBlocker() {
        addCreatureReady(player1, new HolyCow());
        addCreatureReady(player2, new TrainedArynx());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
