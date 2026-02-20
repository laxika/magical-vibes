package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarOfMightTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Avatar of Might has correct card properties")
    void hasCorrectProperties() {
        AvatarOfMight card = new AvatarOfMight();

        assertThat(card.getName()).isEqualTo("Avatar of Might");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{6}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(8);
        assertThat(card.getToughness()).isEqualTo(8);
        assertThat(card.getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ReduceOwnCastCostIfOpponentControlsMoreCreaturesEffect.class);
    }

    @Test
    @DisplayName("Cannot cast Avatar of Might for {G}{G} without cost reduction")
    void cannotCastWithoutReduction() {
        harness.setHand(player1, List.of(new AvatarOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast Avatar of Might for {G}{G} when opponent controls four more creatures")
    void canCastWithReductionAtFourMoreCreatures() {
        harness.setHand(player1, List.of(new AvatarOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Avatar of Might");
    }

    @Test
    @DisplayName("Cost reduction does not apply when opponent controls only three more creatures")
    void reductionDoesNotApplyWithOnlyThreeMoreCreatures() {
        harness.setHand(player1, List.of(new AvatarOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Avatar of Might resolves onto the battlefield with trample")
    void resolvesOntoBattlefieldWithTrample() {
        harness.setHand(player1, List.of(new AvatarOfMight()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Avatar of Might")
                        && p.hasKeyword(Keyword.TRAMPLE));
    }
}
