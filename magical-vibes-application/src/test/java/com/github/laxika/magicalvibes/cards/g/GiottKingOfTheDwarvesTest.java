package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwarvenReinforcements;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HonedKhopesh;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiottKingOfTheDwarves.class, DwarvenReinforcements.class, Forest.class,
        HonedKhopesh.class, GrizzlyBears.class})
class GiottKingOfTheDwarvesTest extends BaseCardTest {

    @Test
    @DisplayName("Giott's own entry lets you discard a card to draw a card")
    void ownDwarfEntryDiscardsAndDraws() {
        GrizzlyBears discarded = new GrizzlyBears();
        GiottKingOfTheDwarves giott = new GiottKingOfTheDwarves();
        Forest drawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        harness.setHand(player1, new ArrayList<>(List.of(discarded, giott)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        resolveLoot(true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Another Dwarf entering triggers Giott")
    void anotherDwarfEntryTriggersGiott() {
        GrizzlyBears discardedOne = new GrizzlyBears();
        GrizzlyBears discardedTwo = new GrizzlyBears();
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(drawnOne, drawnTwo));
        harness.addToBattlefield(player1, new GiottKingOfTheDwarves());
        DwarvenReinforcements reinforcements = new DwarvenReinforcements();
        harness.setHand(player1, new ArrayList<>(List.of(discardedOne, discardedTwo, reinforcements)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 2, 0);
        harness.passBothPriorities();
        resolveLoot(true);
        resolveLoot(true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(reinforcements, discardedOne, discardedTwo);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
    }

    @Test
    @DisplayName("An Equipment entering under your control triggers Giott")
    void equipmentEntryTriggersGiott() {
        GrizzlyBears discarded = new GrizzlyBears();
        HonedKhopesh equipment = new HonedKhopesh();
        Forest drawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        harness.addToBattlefield(player1, new GiottKingOfTheDwarves());
        harness.setHand(player1, new ArrayList<>(List.of(discarded, equipment)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 1);
        harness.passBothPriorities();
        resolveLoot(true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining Giott's trigger does not discard or draw")
    void triggerMayBeDeclined() {
        GrizzlyBears discarded = new GrizzlyBears();
        GiottKingOfTheDwarves giott = new GiottKingOfTheDwarves();
        Forest drawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        harness.setHand(player1, new ArrayList<>(List.of(discarded, giott)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        resolveLoot(false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A non-Dwarf creature entering does not trigger Giott")
    void nonDwarfEntryDoesNotTriggerGiott() {
        harness.addToBattlefield(player1, new GiottKingOfTheDwarves());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveLoot(boolean accept) {
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
        if (accept) {
            harness.handleCardChosen(player1, 0);
        }
    }
}
