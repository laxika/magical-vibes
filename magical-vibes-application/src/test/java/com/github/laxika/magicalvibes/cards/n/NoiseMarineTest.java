package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoiseMarine.class, GrizzlyBears.class, LightningBolt.class})
class NoiseMarineTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade offers a lesser nonland card from the top of the library")
    void cascadeOffersLesserNonlandCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NoiseMarine()));
        addNoiseMarineMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
    }

    @Test
    @DisplayName("The Sonic Blaster trigger deals damage equal to spells cast this turn")
    void etbDealsDamageEqualToSpellsCastThisTurn() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new NoiseMarine()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllStack();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllStack();
        harness.castCreature(player1, 0, player2.getId());
        resolveAllStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    private void addNoiseMarineMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void resolveAllStack() {
        for (int i = 0; i < 12 && (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()); i++) {
            if (gd.interaction.isAwaitingInput()) {
                return;
            }
            harness.passBothPriorities();
        }
    }
}
