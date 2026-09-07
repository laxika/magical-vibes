package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlarggAndNassari.class, Forest.class, GrizzlyBears.class})
class PlarggAndNassariTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles through nonlands, excludes the opponent's choice, and offers the other spell")
    void exilesThroughNonlandsAndOffersTheOtherSpell() {
        Forest player1Land = new Forest();
        GrizzlyBears player1FirstSpell = new GrizzlyBears();
        Forest player2Land = new Forest();
        GrizzlyBears player2Spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Land, player1FirstSpell));
        harness.setLibrary(player2, List.of(player2Land, player2Spell));
        harness.addToBattlefield(player1, new PlarggAndNassari());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PlarggAndNassariCardChoice opponentChoice =
                (PendingInteraction.PlarggAndNassariCardChoice) gd.interaction.activeInteraction();
        assertThat(opponentChoice.opponentId()).isEqualTo(player2.getId());
        assertThat(opponentChoice.validCardIds()).containsExactlyInAnyOrder(
                player1FirstSpell.getId(), player2Spell.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .containsExactlyInAnyOrder(player1Land, player1FirstSpell, player2Land, player2Spell);

        harness.handleMultipleCardsChosen(player2, List.of(player2Spell.getId()));

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(castChoice.validCardIds()).containsExactlyInAnyOrder(
                player1FirstSpell.getId());
        assertThat(castChoice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(player1FirstSpell.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard())
                .containsExactly(player1FirstSpell);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .contains(player2Spell, player1Land, player2Land);
    }
}
