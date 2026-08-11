package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FireboltTest extends BaseCardTest {

    @Test
    @DisplayName("Firebolt deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Firebolt deals 2 damage to target creature")
    void deals2DamageToCreature() {
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new HillGiant());

        UUID creatureId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, creatureId);
        harness.passBothPriorities();

        Permanent permanent = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(creatureId))
                .findFirst().orElseThrow();
        assertThat(permanent.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback from graveyard deals 2 damage and exiles Firebolt")
    void flashbackDealsDamageAndExiles() {
        harness.setGraveyard(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotInGraveyard(player1, "Firebolt");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Firebolt"));
    }

    @Test
    @DisplayName("Cannot cast Firebolt with flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new Firebolt()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
