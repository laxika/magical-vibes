package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiresOfUndeathTest extends BaseCardTest {

    

    @Test
    @DisplayName("Fires of Undeath deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fires of Undeath deals 2 damage to target creature, destroying a 2/2")
    void deals2DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast Fires of Undeath without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Fires of Undeath goes to graveyard after normal cast")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fires of Undeath"));
    }

    @Test
    @DisplayName("Flashback from graveyard deals 2 damage to target player")
    void flashbackDeals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving, not sent to graveyard")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Fires of Undeath"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fires of Undeath"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsSpell() {
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Fires of Undeath");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
