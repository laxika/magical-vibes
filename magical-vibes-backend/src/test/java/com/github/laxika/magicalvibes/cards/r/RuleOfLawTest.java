package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsPerTurnEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleOfLawTest {

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

    // ===== Card properties =====

    @Test
    @DisplayName("Rule of Law has correct card properties")
    void hasCorrectProperties() {
        RuleOfLaw card = new RuleOfLaw();

        assertThat(card.getName()).isEqualTo("Rule of Law");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getCardText()).isEqualTo("Each player can't cast more than one spell each turn.");
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(LimitSpellsPerTurnEffect.class);
        LimitSpellsPerTurnEffect effect = (LimitSpellsPerTurnEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.maxSpells()).isEqualTo(1);
    }

    // ===== Spell limiting =====

    @Test
    @DisplayName("First spell is still castable with Rule of Law on battlefield")
    void allowsFirstSpell() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Second spell is not playable with Rule of Law on battlefield")
    void preventsSecondSpellFromBeingPlayable() {
        harness.addToBattlefield(player1, new RuleOfLaw());

        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.setHand(player1, List.of(bear1, bear2));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast first creature — should succeed
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Try casting second creature — should fail
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Rule of Law affects both players")
    void affectsBothPlayers() {
        harness.addToBattlefield(player1, new RuleOfLaw());

        // Player2 casts one spell
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // Player2 tries second spell — should fail
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Removing Rule of Law restores normal casting")
    void removingRuleOfLawRestoresNormalCasting() {
        harness.addToBattlefield(player1, new RuleOfLaw());

        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.setHand(player1, List.of(bear1, bear2));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast first creature
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Remove Rule of Law from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Rule of Law"));

        // Second creature should now be castable
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Playing a land is not affected by Rule of Law")
    void landsAreNotAffected() {
        harness.addToBattlefield(player1, new RuleOfLaw());

        // Player casts a creature, then plays a land — land should still work
        GrizzlyBears bear = new GrizzlyBears();
        Plains plains = new Plains();
        harness.setHand(player1, List.of(bear, plains));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast creature first
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Play land — should succeed even after casting a spell
        gs.playCard(gd, player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));
    }
}

