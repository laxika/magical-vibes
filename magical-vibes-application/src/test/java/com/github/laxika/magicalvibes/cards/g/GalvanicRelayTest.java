package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GalvanicRelay.class, Shock.class})
class GalvanicRelayTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card and grants play permission until the end of the next turn")
    void exilesTopCardAndGrantsPlayPermission() {
        Card top = new Shock();
        Card remaining = new Shock();
        harness.setLibrary(player1, List.of(top, remaining));
        harness.setHand(player1, List.of(new GalvanicRelay()));
        addRelayMana();

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd)
                .containsEntry(top.getId(), gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Galvanic Relay")
    void stormCreatesCopyForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new Shock());
        gd.recordSpellCast(player2.getId(), new Shock());
        harness.setHand(player1, List.of(new GalvanicRelay()));
        addRelayMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Each Storm copy resolves Galvanic Relay's library effect")
    void stormCopiesExileOneCardEach() {
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        gd.recordSpellCast(player1.getId(), new Shock());
        harness.setHand(player1, List.of(new GalvanicRelay()));
        addRelayMana();

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    private void addRelayMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
