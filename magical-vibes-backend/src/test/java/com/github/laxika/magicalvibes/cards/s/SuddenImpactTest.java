package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuddenImpactTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Sudden Impact has correct card properties")
    void hasCorrectProperties() {
        SuddenImpact card = new SuddenImpact();

        assertThat(card.getName()).isEqualTo("Sudden Impact");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{3}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DealDamageToTargetPlayerByHandSizeEffect.class);
    }

    @Test
    @DisplayName("Casting Sudden Impact targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sudden Impact");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Sudden Impact deals damage equal to target player's hand size")
    void dealsDamageEqualToHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.setHand(player2, List.of(new Plains(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Sudden Impact uses target player's hand size on resolution")
    void usesHandSizeOnResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.setHand(player2, List.of(new Plains(), new Island()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Sudden Impact deals 0 damage if target player has no cards in hand")
    void dealsZeroDamageWithEmptyHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sudden Impact cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new SuddenImpact()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
