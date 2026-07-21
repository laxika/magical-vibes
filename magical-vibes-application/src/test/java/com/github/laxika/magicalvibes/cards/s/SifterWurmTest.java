package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SifterWurmTest extends BaseCardTest {

    private void castSifterWurm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SifterWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB on stack
        harness.passBothPriorities(); // resolve ETB → scry begins
    }

    @Test
    @DisplayName("ETB enters scry state with 3 cards")
    void etbEntersScry3() {
        Card a = new Forest();
        Card b = new Forest();
        Card c = new Forest();
        Card d = new GrizzlyBears();
        harness.setLibrary(player1, List.of(a, b, c, d));

        castSifterWurm();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("After scry, reveals new top card and gains life equal to its mana value; card stays on top")
    void afterScryGainsLifeEqualToTopManaValue() {
        Card top = new GrizzlyBears(); // MV 2
        Card mid = new Forest();
        Card bottom = new Forest();
        Card rest = new Forest();
        harness.setLibrary(player1, List.of(top, mid, bottom, rest));
        harness.setLife(player1, 20);

        castSifterWurm();

        // Keep all three on top in original order, then reveal top (Grizzly Bears, MV 2)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("Scry reorder changes which card's mana value grants life")
    void scryReorderAffectsLifeGain() {
        Card a = new Forest(); // MV 0
        Card b = new Forest(); // MV 0
        Card c = new Shock(); // MV 1
        Card d = new Forest();
        harness.setLibrary(player1, List.of(a, b, c, d));
        harness.setLife(player1, 20);

        castSifterWurm();

        // Put Shock on top (index 2 of the scried cards)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(2), List.of(0, 1)));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(c);
    }

    @Test
    @DisplayName("Empty library skips scry interaction and does not change life")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        castSifterWurm();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
