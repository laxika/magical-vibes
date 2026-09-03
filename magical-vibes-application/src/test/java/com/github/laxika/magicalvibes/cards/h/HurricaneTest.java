package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Hurricane.class, AirElemental.class, GrizzlyBears.class})
class HurricaneTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Hurricane puts it on the stack as a sorcery spell")
    void castingHurricanePutsItOnStack() {
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();

        // Hurricane is on the stack as a sorcery spell
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getXValue()).isEqualTo(3);

        // Hand is now empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        // Mana was spent ({X}{G} with X=3 → 4G total)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Hurricane resolves dealing X damage to all players")
    void hurricaneResolvesDealsXDamageToPlayers() {
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();

        // Stack is empty after resolution
        assertThat(gd.stack).isEmpty();

        // Both players lost 3 life (20 → 17)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Hurricane kills flying creatures")
    void hurricaneKillsFlyingCreatures() {
        // Put 4/4 flyers on both battlefields.
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new AirElemental());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castAndResolveSorcery(player1, 0, 4);

        // Both flying creatures should be destroyed (4 damage >= 4 toughness).
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Hurricane does not kill non-flying creatures")
    void hurricaneDoesNotKillNonFlyingCreatures() {
        // Put a non-flying creature on opponent's battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 3);

        // Non-flying creature survives
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Hurricane with X=0 deals no damage")
    void hurricaneWithXZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();

        // Both players stay at 20 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot cast Hurricane without enough mana for X + colored cost")
    void cannotCastHurricaneWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Hurricane can kill the caster")
    void hurricaneCanKillCaster() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();

        // Caster took 3 damage (3 → 0), game should be over
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}

