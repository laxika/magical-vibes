package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromColorsEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LuminesceTest {

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

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card(name, CardType.CREATURE, "{1}", color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Luminesce has correct card properties")
    void hasCorrectProperties() {
        Luminesce card = new Luminesce();

        assertThat(card.getName()).isEqualTo("Luminesce");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(PreventDamageFromColorsEffect.class);

        PreventDamageFromColorsEffect effect = (PreventDamageFromColorsEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.colors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Luminesce puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, "W", 1);

        harness.castInstant(player1, 0, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Luminesce");
    }

    @Test
    @DisplayName("Cannot cast Luminesce without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new Luminesce()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Luminesce adds black and red to prevented colors")
    void resolvingAddsPreventedColors() {
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, "W", 1);

        harness.castInstant(player1, 0, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.preventDamageFromColors).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
    }

    @Test
    @DisplayName("Luminesce goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Luminesce()));
        harness.addMana(player1, "W", 1);

        harness.castInstant(player1, 0, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Luminesce"));
    }

    // ===== Combat damage prevention - black source =====

    @Test
    @DisplayName("Prevents combat damage from black creature to player")
    void preventsBlackCreatureDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = new Permanent(createCreature("Black Knight", 2, 2, CardColor.BLACK));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage from black creature to blocking creature")
    void preventsBlackCreatureDamageToBlocker() {
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = new Permanent(createCreature("Black Knight", 3, 3, CardColor.BLACK));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Black attacker's 3 damage is prevented, so blocker (2/2) survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Blocker still deals 2 damage to attacker (green is not prevented), but 2 < 3 toughness → survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));
    }

    // ===== Combat damage prevention - red source =====

    @Test
    @DisplayName("Prevents combat damage from red creature to player")
    void preventsRedCreatureDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = new Permanent(createCreature("Goblin Raider", 2, 1, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Non-prevented colors still deal damage =====

    @Test
    @DisplayName("Does not prevent combat damage from green creature")
    void doesNotPreventGreenCreatureDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Green creature's 2 damage goes through
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent combat damage from white creature")
    void doesNotPreventWhiteCreatureDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        Permanent attacker = new Permanent(createCreature("White Knight", 2, 2, CardColor.WHITE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Mixed combat =====

    @Test
    @DisplayName("In mixed combat, only prevents damage from black/red attackers")
    void mixedCombatOnlyPreventsBlackRedDamage() {
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        // Black attacker (prevented)
        Permanent blackAttacker = new Permanent(createCreature("Black Knight", 2, 2, CardColor.BLACK));
        blackAttacker.setSummoningSick(false);
        blackAttacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(blackAttacker);

        // Green attacker (not prevented)
        Permanent greenAttacker = new Permanent(new GrizzlyBears());
        greenAttacker.setSummoningSick(false);
        greenAttacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(greenAttacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Only green's 2 damage goes through, black's 2 is prevented
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Spell damage prevention =====

    @Test
    @DisplayName("Does not prevent spell damage from non-prevented color (Hurricane is green)")
    void doesNotPreventGreenSpellDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.h.Hurricane()));
        harness.addMana(player1, "G", 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hurricane is green, so damage is not prevented
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Prevented colors are cleared at end of turn")
    void preventedColorsClearedAtEndOfTurn() {
        harness.getGameData().preventDamageFromColors.add(CardColor.BLACK);
        harness.getGameData().preventDamageFromColors.add(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.preventDamageFromColors).isEmpty();
    }
}
