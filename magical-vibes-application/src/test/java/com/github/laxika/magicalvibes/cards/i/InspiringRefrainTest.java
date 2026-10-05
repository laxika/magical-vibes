package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ClaraOswald;
import com.github.laxika.magicalvibes.cards.p.PastInFlames;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringRefrain.class, ClaraOswald.class, PastInFlames.class})
class InspiringRefrainTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and exiles Inspiring Refrain with three suspend time counters")
    void drawsTwoCardsAndExilesWithSuspendCounters() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setLibrary(player1, List.of(new ClaraOswald(), new ClaraOswald()));
        harness.setHand(player1, List.of(refrain));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof ClaraOswald);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Suspend casts Inspiring Refrain for free after three upkeeps")
    void suspendCastsForFreeAfterThreeUpkeeps() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setLibrary(player1, List.of(new ClaraOswald(), new ClaraOswald()));
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
                .allMatch(card -> card instanceof ClaraOswald);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("A resolved Refrain casts again after three owner upkeeps")
    void resolvedRefrainRepeatsSuspendCycle() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setLibrary(player1, List.of(new ClaraOswald(), new ClaraOswald(),
                new ClaraOswald(), new ClaraOswald()));
        harness.setHand(player1, List.of(refrain));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Refrain exiled without time counters")
    void decliningSuspendCastLeavesCardExiled() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setHand(player1, List.of(refrain));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(refrain.getId());
        assertThat(gd.suspendedSpellExiles).isEmpty();
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Refrain cast with flashback still exiles itself with three time counters")
    void flashbackPreservesSelfExileTimeCounters() {
        InspiringRefrain refrain = new InspiringRefrain();
        harness.setLibrary(player1, List.of(new ClaraOswald(), new ClaraOswald()));
        harness.setGraveyard(player1, List.of(refrain));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(refrain);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(refrain.getId(), player1.getId(), 3));
    }
}
