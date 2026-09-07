package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScrybSprites;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Earthquake.class, GrizzlyBears.class, ScrybSprites.class})
class EarthquakeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Earthquake puts it on the stack as a sorcery spell")
    void castingEarthquakePutsItOnStack() {
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getXValue()).isEqualTo(3);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Earthquake resolves dealing X damage to all players")
    void earthquakeResolvesDealsXDamageToPlayers() {
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Earthquake kills non-flying creatures")
    void earthquakeKillsNonFlyingCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Earthquake does not damage flying creatures")
    void earthquakeDoesNotDamageFlyingCreatures() {
        harness.addToBattlefield(player2, new ScrybSprites());

        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Scryb Sprites");
    }

    @Test
    @DisplayName("Earthquake with X=0 deals no damage")
    void earthquakeWithXZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot cast Earthquake without enough mana for X + colored cost")
    void cannotCastEarthquakeWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Earthquake can kill the caster")
    void earthquakeCanKillCaster() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
