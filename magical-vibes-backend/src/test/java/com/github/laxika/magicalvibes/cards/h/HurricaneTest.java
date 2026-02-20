package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HurricaneTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    /** A 2/2 flying creature for test purposes. */
    private static Card flyingCreature() {
        Card card = new Card();
        card.setName("Wind Drake");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}{U}");
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

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
        assertThat(entry.getCard().getName()).isEqualTo("Hurricane");
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
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

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
        // Put a 2/2 flyer on opponent's battlefield
        harness.addToBattlefield(player2, flyingCreature());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 2);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Flying creature should be destroyed (2 damage >= 2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wind Drake"));
    }

    @Test
    @DisplayName("Hurricane does not kill non-flying creatures")
    void hurricaneDoesNotKillNonFlyingCreatures() {
        // Put a non-flying creature on opponent's battlefield
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Non-flying creature survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Hurricane with X=0 deals no damage")
    void hurricaneWithXZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);

        harness.passBothPriorities();

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
        harness.castSorcery(player1, 0, 3);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Caster took 3 damage (3 → 0), game should be over
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}

