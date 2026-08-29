package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimalGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, it puts one basic land onto the battlefield")
    void putsOneBasicLandOntoBattlefieldWithoutKicker() {
        castPrimalGrowth();
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(1);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Primal Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With kicker, it sacrifices a creature and puts up to two basic lands onto the battlefield")
    void sacrificesCreatureAndPutsTwoBasicLandsOntoBattlefieldWithKicker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Primal Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kicker requires a creature to sacrifice")
    void kickerRequiresCreatureToSacrifice() {
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private void castPrimalGrowth() {
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();
        harness.castSorcery(player1, 0, 0);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
