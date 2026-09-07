package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HideSeek.class, FountainOfYouth.class, GrizzlyBears.class})
class HideSeekTest extends BaseCardTest {

    private static final int HIDE = 0;
    private static final int SEEK = 1;

    @Test
    @DisplayName("Hide puts a target artifact on the bottom of its owner's library")
    void hidePutsArtifactOnBottomOfLibrary() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, HIDE, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, artifact.getCard());
    }

    @Test
    @DisplayName("Hide cannot target a creature")
    void hideCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, HIDE, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Seek exiles a card from an opponent's library and gains its mana value")
    void seekExilesCardAndGainsManaValueAsLife() {
        GrizzlyBears chosen = new GrizzlyBears();
        harness.setLibrary(player2, List.of(chosen));
        harness.setHand(player1, List.of(new HideSeek()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, SEEK, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(chosen);
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
