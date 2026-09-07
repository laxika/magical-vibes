package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ArcBlade.class)
class ArcBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target player and is exiled with three time counters")
    void dealsDamageAndIsExiledWithSuspendCounters() {
        ArcBlade blade = new ArcBlade();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(blade));
        addCastMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(blade.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Suspend exiles Arc Blade with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        ArcBlade blade = new ArcBlade();
        harness.setHand(player1, List.of(blade));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
        assertThat(gd.exiledCardTimeCounters).containsEntry(blade.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A suspended Arc Blade deals damage when cast for free")
    void suspendedCardCastsForFree() {
        ArcBlade blade = new ArcBlade();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(blade));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(blade.getId(), player1.getId(), 3));
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
