package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RequirePhyrexianPaymentToAttackEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NornsAnnexTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Norn's Annex has RequirePhyrexianPaymentToAttackEffect with WHITE")
    void hasCorrectEffect() {
        NornsAnnex card = new NornsAnnex();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(RequirePhyrexianPaymentToAttackEffect.class);
        RequirePhyrexianPaymentToAttackEffect effect =
                (RequirePhyrexianPaymentToAttackEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.color()).isEqualTo(ManaColor.WHITE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Norn's Annex puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new NornsAnnex()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Norn's Annex"));
    }

    // ===== Attack tax — pay with white mana =====

    @Test
    @DisplayName("Opponent can attack by paying white mana for each attacker")
    void attackerPaysWhiteMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player 1 controls Norn's Annex
        Permanent annex = new Permanent(new NornsAnnex());
        gd.playerBattlefields.get(player1.getId()).add(annex);

        // Player 2 has a creature
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // Player 2 has white mana to pay the tax
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player2, List.of(0));

        // White mana should be spent
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        // No life loss from tax
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Attack tax — pay with life =====

    @Test
    @DisplayName("Opponent pays 2 life per attacker when no white mana available")
    void attackerPaysLifeWhenNoWhiteMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player 1 controls Norn's Annex
        Permanent annex = new Permanent(new NornsAnnex());
        gd.playerBattlefields.get(player1.getId()).add(annex);

        // Player 2 has a creature but no white mana
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player2, List.of(0));

        // 2 life paid for Phyrexian tax
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Attack tax — multiple attackers =====

    @Test
    @DisplayName("Each attacker costs {W/P} — multiple attackers pay multiple times")
    void multipleAttackersCostMultipleTax() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent annex = new Permanent(new NornsAnnex());
        gd.playerBattlefields.get(player1.getId()).add(annex);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        // Only 1 white mana — pays for one, life for the other
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player2, List.of(0, 1));

        // 1 white mana spent + 2 life for the second attacker
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Declaring no attackers is valid =====

    @Test
    @DisplayName("Player can choose not to attack when Norn's Annex is on the battlefield")
    void canDeclareNoAttackers() {
        harness.setLife(player2, 20);

        Permanent annex = new Permanent(new NornsAnnex());
        gd.playerBattlefields.get(player1.getId()).add(annex);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Does not affect controller's creatures =====

    @Test
    @DisplayName("Norn's Annex does not tax its controller's attackers")
    void doesNotTaxController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player 1 controls Norn's Annex and a creature
        Permanent annex = new Permanent(new NornsAnnex());
        gd.playerBattlefields.get(player1.getId()).add(annex);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(1));

        // No life loss, no mana cost for controller
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Must-attack exemption =====

    @Test
    @DisplayName("Must-attack creatures are not forced to attack when Norn's Annex imposes a tax")
    void mustAttackExemptionWithPhyrexianTax() {
        harness.setLife(player2, 20);

        Permanent annex = new Permanent(new NornsAnnex());
        gd.playerBattlefields.get(player1.getId()).add(annex);

        // Player 2 has a must-attack creature (Bloodrock Cyclops)
        com.github.laxika.magicalvibes.cards.b.BloodrockCyclops cyclops =
                new com.github.laxika.magicalvibes.cards.b.BloodrockCyclops();
        Permanent cycloPerm = new Permanent(cyclops);
        cycloPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(cycloPerm);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Declaring no attackers should succeed (tax exempts must-attack)
        gs.declareAttackers(gd, player2, List.of());

        assertThat(cycloPerm.isAttacking()).isFalse();
    }

    // ===== All attackers pay 2 life with no white mana =====

    @Test
    @DisplayName("Three attackers with no white mana costs 6 life total")
    void threeAttackersPaySixLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent annex = new Permanent(new NornsAnnex());
        gd.playerBattlefields.get(player1.getId()).add(annex);

        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(bears);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player2, List.of(0, 1, 2));

        // 3 * 2 life = 6 life total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
