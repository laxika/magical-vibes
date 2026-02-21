package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PincherBeetlesTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Pincher Beetles has correct card properties")
    void hasCorrectProperties() {
        PincherBeetles card = new PincherBeetles();

        assertThat(card.getName()).isEqualTo("Pincher Beetles");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.INSECT);
        assertThat(card.getKeywords()).containsExactly(Keyword.SHROUD);
    }

    @Test
    @DisplayName("Casting Pincher Beetles puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PincherBeetles()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Pincher Beetles");
    }

    @Test
    @DisplayName("Opponent spells cannot target Pincher Beetles")
    void opponentSpellsCannotTarget() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new PincherBeetles());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd,
                player2,
                0,
                0,
                harness.getPermanentId(player1, "Pincher Beetles"),
                null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Opponent activated abilities cannot target Pincher Beetles")
    void opponentAbilitiesCannotTarget() {
        Permanent beetles = new Permanent(new PincherBeetles());
        beetles.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(beetles);

        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancer);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, beetles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Your own spells cannot target Pincher Beetles")
    void ownSpellsCannotTarget() {
        harness.addToBattlefield(player1, new PincherBeetles());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd,
                player1,
                0,
                0,
                harness.getPermanentId(player1, "Pincher Beetles"),
                null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Your own activated abilities cannot target Pincher Beetles")
    void ownAbilitiesCannotTarget() {
        Permanent beetles = new Permanent(new PincherBeetles());
        beetles.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(beetles);

        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, beetles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
