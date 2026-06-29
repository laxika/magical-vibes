package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AngrathsMaraudersTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Angrath's Marauders has correct static effect")
    void hasCorrectEffect() {
        AngrathsMarauders card = new AngrathsMarauders();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(DoubleControllerDamageEffect.class);
        DoubleControllerDamageEffect effect = (DoubleControllerDamageEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.stackFilter()).isNull();
        assertThat(effect.appliesToCombatDamage()).isTrue();
    }

    // ===== Doubles spell damage to player =====

    @Test
    @DisplayName("Doubles Shock damage to a player")
    void doublesSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Doubles spell damage to creature =====

    @Test
    @DisplayName("Doubled spell damage destroys a creature that would survive base damage")
    void doublesSpellDamageToCreature() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.addToBattlefield(player2, new SerraAngel()); // 4/4
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, serraId);
        harness.passBothPriorities();

        // 2 damage doubled to 4 — kills Serra Angel (4/4)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    // ===== Doubles combat damage =====

    @Test
    @DisplayName("Doubles unblocked combat damage to player")
    void doublesUnblockedCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AngrathsMarauders());

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(1)); // bear is at index 1 (Marauders at 0)

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubled combat damage kills blocker that would survive base damage")
    void doublesCombatDamageKillsBlocker() {
        harness.addToBattlefield(player1, new AngrathsMarauders());

        // 2/2 attacker
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // 4/4 blocker — base 2 damage wouldn't kill it, but doubled 4 does
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        // Serra Angel (4/4) takes 2*2=4 doubled damage — exactly lethal
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
    }

    // ===== Only doubles controller's damage =====

    @Test
    @DisplayName("Does not double opponent's spell damage")
    void doesNotDoubleOpponentsSpellDamage() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // 2 damage — NOT doubled (opponent's spell, not Marauders controller's)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent's Marauders does not double your combat damage")
    void opponentsMaraudersDoesNotDoubleYourCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AngrathsMarauders());

        // Player1's creature attacks — should NOT be doubled by opponent's Marauders
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        bear.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        // Defender has a creature (Marauders) so combat pauses for blockers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of()); // no blockers
        harness.passBothPriorities();

        // 2 combat damage — NOT doubled (opponent's Marauders, not yours)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Two Marauders stack multiplicatively =====

    @Test
    @DisplayName("Two Angrath's Marauders quadruple spell damage")
    void twoMaraudersQuadrupleSpellDamage() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 * 2 = 8 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Two Angrath's Marauders quadruple combat damage")
    void twoMaraudersQuadrupleCombatDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.addToBattlefield(player1, new AngrathsMarauders());

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(2)); // bear at index 2 (two Marauders at 0, 1)

        // 2 combat damage * 4 = 8
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    // ===== Removing stops doubling =====

    @Test
    @DisplayName("Removing Angrath's Marauders from battlefield stops doubling")
    void removingStopsDoubling() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setLife(player2, 20);

        // Deal doubled damage first
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 * 2 = 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Remove Marauders from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Angrath's Marauders"));

        // Deal damage again without Marauders
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // 2 damage (not doubled), life goes from 16 to 14
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    // ===== Doubles X damage from sorcery =====

    @Test
    @DisplayName("Doubles X damage from Blaze to a player")
    void doublesXDamageFromSorcery() {
        harness.addToBattlefield(player1, new AngrathsMarauders());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
