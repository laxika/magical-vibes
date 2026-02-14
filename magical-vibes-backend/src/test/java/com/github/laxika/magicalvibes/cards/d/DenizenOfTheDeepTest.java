package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DenizenOfTheDeepTest {

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

    // ===== Card properties =====

    @Test
    @DisplayName("Denizen of the Deep has correct card properties")
    void hasCorrectProperties() {
        DenizenOfTheDeep card = new DenizenOfTheDeep();

        assertThat(card.getName()).isEqualTo("Denizen of the Deep");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{6}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(11);
        assertThat(card.getToughness()).isEqualTo(11);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SERPENT);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ReturnCreaturesToOwnersHandEffect.class);
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("ETB triggers when Denizen enters the battlefield")
    void etbTriggersOnEnter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DenizenOfTheDeep()));
        harness.addMana(player1, "U", 8);

        harness.castCreature(player1, 0);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Denizen of the Deep"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Denizen of the Deep");
    }

    @Test
    @DisplayName("ETB returns all other creatures controller owns to hand")
    void etbReturnsAllOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.setHand(player1, List.of(new DenizenOfTheDeep()));
        harness.addMana(player1, "U", 8);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Only Denizen should remain on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(p -> p.getCard().getName().equals("Denizen of the Deep"));

        // Grizzly Bears and Serra Angel should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Serra Angel");
    }

    @Test
    @DisplayName("ETB does not return opponent's creatures")
    void etbDoesNotReturnOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new DenizenOfTheDeep()));
        harness.addMana(player1, "U", 8);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Opponent's Serra Angel should still be on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serra Angel"));

        // Player 1's Grizzly Bears should be bounced to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB with no other creatures does nothing")
    void etbWithNoOtherCreaturesDoesNothing() {
        harness.setHand(player1, List.of(new DenizenOfTheDeep()));
        harness.addMana(player1, "U", 8);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Only Denizen on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1)
                .allMatch(p -> p.getCard().getName().equals("Denizen of the Deep"));

        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
