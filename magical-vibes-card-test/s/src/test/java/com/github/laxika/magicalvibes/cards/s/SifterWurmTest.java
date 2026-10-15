package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SifterWurm.class, Forest.class, FeralProwler.class, Unsummon.class})
class SifterWurmTest extends BaseCardTest {

    private void castSifterWurm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SifterWurm(), "{5}{G}{G}");
        harness.passBothPriorities(); // resolve creature → ETB on stack
        harness.passBothPriorities(); // resolve ETB → scry begins
    }

    @Test
    @DisplayName("ETB enters scry state with 3 cards")
    void etbEntersScry3() {
        Card a = new Forest();
        Card b = new Forest();
        Card c = new Forest();
        Card d = new FeralProwler();
        harness.setLibrary(player1, List.of(a, b, c, d));

        castSifterWurm();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("After scry, reveals new top card and gains life equal to its mana value; card stays on top")
    void afterScryGainsLifeEqualToTopManaValue() {
        Card top = new FeralProwler(); // MV 2
        Card mid = new Forest();
        Card bottom = new Forest();
        Card rest = new Forest();
        harness.setLibrary(player1, List.of(top, mid, bottom, rest));
        harness.setLife(player1, 20);

        castSifterWurm();

        // Keep all three on top in original order, then reveal top (Feral Prowler, MV 2)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("Scry reorder changes which card's mana value grants life")
    void scryReorderAffectsLifeGain() {
        Card a = new Forest(); // MV 0
        Card b = new Forest(); // MV 0
        Card c = new Unsummon(); // MV 1
        Card d = new Forest();
        harness.setLibrary(player1, List.of(a, b, c, d));
        harness.setLife(player1, 20);

        castSifterWurm();

        // Put Unsummon on top (index 2 of the scried cards)
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

    @Test
    @DisplayName("Bottoming all three scried cards reveals the previously fourth card")
    void bottomingAllCardsRevealsFourthCard() {
        Card a = new Forest();
        Card b = new Unsummon();
        Card c = new FeralProwler();
        Card fourth = new SifterWurm();
        harness.setLibrary(player1, List.of(a, b, c, fourth));
        harness.setLife(player1, 20);

        castSifterWurm();

        harness.assertLife(player1, 20);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(2, 1, 0)));

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, c, b, a);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Revealing a land gains no life and leaves it on top")
    void revealingLandGainsNoLife() {
        Card land = new Forest();
        Card spell = new FeralProwler();
        harness.setLibrary(player1, List.of(land, spell));
        harness.setLife(player1, 20);

        castSifterWurm();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(land, spell);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bottoming the only card still reveals that card and gains its mana value")
    void bottomingOnlyCardStillRevealsIt() {
        Card onlyCard = new FeralProwler();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setLife(player1, 20);

        castSifterWurm();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.stack).isEmpty();
    }
}
