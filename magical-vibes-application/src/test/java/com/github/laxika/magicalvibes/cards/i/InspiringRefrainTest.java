package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InspiringRefrain.class)
class InspiringRefrainTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and is exiled with three time counters")
    void drawsTwoCardsAndIsExiledWithSuspendCounters() {
        InspiringRefrain refrain = new InspiringRefrain();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(refrain));
        addCastMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Suspend casts it for free and starts a new countdown")
    void suspendCastsForFreeAndStartsNewCountdown() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
