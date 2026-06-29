package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SafePassageTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Safe Passage has correct effect")
    void hasCorrectEffect() {
        SafePassage card = new SafePassage();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(PreventAllDamageToControllerAndCreaturesEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Safe Passage puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Safe Passage");
    }

    @Test
    @DisplayName("Cannot cast Safe Passage without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new SafePassage()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Safe Passage adds controller to playersWithAllDamagePrevented")
    void resolvingAddsControllerToPrevented() {
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playersWithAllDamagePrevented).contains(player1.getId());
    }

    @Test
    @DisplayName("Safe Passage goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new SafePassage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Safe Passage"));
    }

    // ===== Prevents combat damage to player =====

    @Test
    @DisplayName("Prevents combat damage to controller from attacking creature")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent attacker = new Permanent(createCreature("Bear", 3, 3));
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

    // ===== Prevents combat damage to creatures =====

    @Test
    @DisplayName("Prevents combat damage to controller's blocking creature")
    void preventsCombatDamageToBlockingCreature() {
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent attacker = new Permanent(createCreature("Big Bear", 5, 5));
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
        // 5 damage to blocker is prevented, so Grizzly Bears (2/2) survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Does not prevent damage to opponent =====

    @Test
    @DisplayName("Does not prevent damage to opponent player")
    void doesNotPreventDamageToOpponent() {
        harness.setLife(player1, 20);
        // Only player2 has Safe Passage protection
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent attacker = new Permanent(createCreature("Bear", 3, 3));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player1 doesn't have protection, takes 3 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Does not prevent damage to opponent's creatures =====

    @Test
    @DisplayName("Does not prevent damage to opponent's creatures")
    void doesNotPreventDamageToOpponentsCreatures() {
        // Only player1 has Safe Passage protection
        harness.getGameData().playersWithAllDamagePrevented.add(player1.getId());

        Permanent attacker = new Permanent(createCreature("Big Bear", 5, 5));
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
        // Grizzly Bears (2/2) takes 5 damage — not protected, should die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Prevents spell damage to player =====

    @Test
    @DisplayName("Prevents spell damage to controller")
    void preventsSpellDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Prevents spell damage to creature =====

    @Test
    @DisplayName("Prevents spell damage to controller's creature")
    void preventsSpellDamageToCreature() {
        harness.getGameData().playersWithAllDamagePrevented.add(player2.getId());

        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Grizzly Bears (2/2) takes 2 damage from Shock, but it's prevented
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Clears at end of turn =====

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        harness.getGameData().playersWithAllDamagePrevented.add(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playersWithAllDamagePrevented).isEmpty();
    }
}
