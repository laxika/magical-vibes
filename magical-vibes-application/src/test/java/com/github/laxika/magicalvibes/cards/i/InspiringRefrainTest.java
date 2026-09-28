package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringRefrain.class, GrizzlyBears.class})
class InspiringRefrainTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and exiles Inspiring Refrain with three suspend time counters")
    void drawsTwoCardsAndExilesWithSuspendCounters() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(refrain));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Suspend casts Inspiring Refrain for free after three upkeeps")
    void suspendCastsForFreeAfterThreeUpkeeps() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(refrain));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.exiledCardTimeCounters).containsEntry(refrain.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }
}
