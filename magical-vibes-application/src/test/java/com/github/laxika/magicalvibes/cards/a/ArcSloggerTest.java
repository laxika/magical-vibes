package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Arc-Slogger")
class ArcSloggerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles ten cards and deals 2 damage to a target creature")
    void exilesTenAndDamagesTargetCreature() {
        harness.addToBattlefield(player1, new ArcSlogger());
        harness.addToBattlefield(player2, new GrizzlyBears());
        GameData gd = harness.getGameData();
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int exileBefore = gd.exiledCards.size();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 10);
        assertThat(gd.exiledCards).hasSize(exileBefore + 10);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 2 damage to a target player")
    void damagesTargetPlayer() {
        harness.addToBattlefield(player1, new ArcSlogger());
        harness.setLife(player2, 20);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate with fewer than ten cards in the library")
    void cannotActivateWithShortLibrary() {
        harness.addToBattlefield(player1, new ArcSlogger());
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new ArcSlogger());
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
