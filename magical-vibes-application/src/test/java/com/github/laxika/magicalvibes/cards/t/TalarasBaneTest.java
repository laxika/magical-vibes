package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArchonOfJustice;
import com.github.laxika.magicalvibes.cards.g.GlenElendraArchmage;
import com.github.laxika.magicalvibes.cards.h.HotheadedGiant;
import com.github.laxika.magicalvibes.cards.p.Primalcrux;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.w.WickerboughElder;
import com.github.laxika.magicalvibes.cards.w.WistfulSelkie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        TalarasBane.class,
        WickerboughElder.class,
        HotheadedGiant.class,
        ArchonOfJustice.class,
        GlenElendraArchmage.class,
        RavensCrime.class,
        WistfulSelkie.class,
        Primalcrux.class
})
class TalarasBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Caster gains life equal to the chosen green creature's toughness, then it is discarded")
    void gainsLifeEqualToToughnessThenDiscards() {
        harness.setHand(player2, List.of(new WickerboughElder(), new HotheadedGiant()));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        // Only the green creature (Wickerbough Elder) is a legal choice; the red Hotheaded Giant is filtered out.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Wickerbough Elder is 4/4 — gain 4 life.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
        harness.assertInGraveyard(player2, "Wickerbough Elder");
        harness.assertNotInHand(player2, "Wickerbough Elder");
    }

    @Test
    @DisplayName("White and multicolored green creatures are valid; other cards are filtered out")
    void eligibleCreatureColorsAreFilteredAndWhiteCanBeChosen() {
        harness.setHand(player2, List.of(
                new ArchonOfJustice(), new GlenElendraArchmage(), new WickerboughElder(),
                new RavensCrime(), new WistfulSelkie()));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Archon of Justice (white), Wickerbough Elder (green), and Wistful Selkie (green/blue)
        // are legal. The blue Glen Elendra Archmage and black Raven's Crime are not.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2, 4);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
        harness.assertInGraveyard(player2, "Archon of Justice");
        harness.assertNotInHand(player2, "Archon of Justice");
    }

    @Test
    @DisplayName("No green or white creature in hand: no life gain, no discard")
    void noValidCreatureDoesNothing() {
        harness.setHand(player2, List.of(new HotheadedGiant(), new GlenElendraArchmage()));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature's characteristic-defining toughness applies while it is in hand")
    void gainsLifeFromCharacteristicDefiningToughnessInHand() {
        harness.addToBattlefield(player2, new WistfulSelkie());
        harness.addToBattlefield(player1, new WickerboughElder());
        harness.setHand(player2, List.of(new Primalcrux()));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, startingLife + 3);
        harness.assertInGraveyard(player2, "Primalcrux");
        harness.assertNotInHand(player2, "Primalcrux");
    }

    @Test
    void multicoloredCreatureCanBeDiscardedForItsToughness() {
        harness.setHand(player2, List.of(new WistfulSelkie(), new ArchonOfJustice()));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, startingLife + 2);
        harness.assertInGraveyard(player2, "Wistful Selkie");
        harness.assertNotInHand(player2, "Wistful Selkie");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void emptyHandDoesNotRequireAChoiceOrGainLife() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, startingLife);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
