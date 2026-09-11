package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoredPegasus.class, GrizzlyBears.class, Hurricane.class, WindSpirit.class})
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

        // Mana was spent ({X}{G} with X=3 â†’ 4G total)
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

        // Both players lost 3 life (20 â†’ 17)
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Hurricane kills flying creatures")
    void hurricaneKillsFlyingCreatures() {
        // Put a flying creature on opponent's battlefield
        harness.addToBattlefield(player2, new WindSpirit());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 2);

        // Flying creature should be destroyed (2 damage >= 2 toughness)
        harness.assertNotOnBattlefield(player2, "Wind Spirit");
    }

    @Test
    @DisplayName("Hurricane deals exactly X damage to a flying creature")
    void hurricaneDealsExactlyXDamageToFlyingCreatures() {
        harness.addToBattlefield(player2, new WindSpirit());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player2, "Wind Spirit");
        assertThat(findPermanent(player2, "Wind Spirit").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Hurricane damages flying creatures controlled by either player")
    void hurricaneDamagesFlyingCreaturesControlledByEitherPlayer() {
        harness.addToBattlefield(player1, new WindSpirit());
        harness.addToBattlefield(player2, new WindSpirit());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertNotOnBattlefield(player1, "Wind Spirit");
        harness.assertNotOnBattlefield(player2, "Wind Spirit");
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

        // Both players stay at 20 life
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
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

        // Caster took 3 damage (3 â†’ 0), game should be over
        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Hurricane deals damage without destroying a flying creature with greater toughness")
    void hurricaneDealsNonlethalDamageToFlyingCreature() {
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new ArmoredPegasus());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player2, "Armored Pegasus");
        assertThat(flyingCreature.getMarkedDamage()).isEqualTo(1);
    }
}
