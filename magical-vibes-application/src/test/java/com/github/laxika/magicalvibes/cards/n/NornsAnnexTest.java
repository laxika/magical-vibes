package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BloodrockCyclops;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NornsAnnex.class, GrizzlyBears.class, BloodrockCyclops.class, KarnLiberated.class,
        InvasionOfZendikar.class})
class NornsAnnexTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Norn's Annex puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new NornsAnnex()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Norn's Annex");
    }

    @Test
    @DisplayName("Opponent can attack by paying white mana for each attacker")
    void attackerPaysWhiteMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player 1 controls Norn's Annex
        harness.addToBattlefield(player1, new NornsAnnex());

        // Player 2 has a creature
        addCreatureReady(player2, new GrizzlyBears());

        // Player 2 has white mana to pay the tax
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackers(player2, List.of(0));

        // White mana should be spent
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        // No life loss from tax
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent pays 2 life per attacker when no white mana available")
    void attackerPaysLifeWhenNoWhiteMana() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player 1 controls Norn's Annex
        harness.addToBattlefield(player1, new NornsAnnex());

        // Player 2 has a creature but no white mana
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        // 2 life paid for Phyrexian tax
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent cannot attack when they cannot pay the Phyrexian life cost")
    void cannotAttackWithoutEnoughLife() {
        harness.setLife(player2, 1);

        harness.addToBattlefield(player1, new NornsAnnex());

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
        assertThat(bears.isAttacking()).isFalse();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent may pay their last 2 life and then loses to state-based actions")
    void payingLastTwoLifeEndsGameCleanly() {
        harness.setLife(player2, 2);

        harness.addToBattlefield(player1, new NornsAnnex());

        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Each attacker costs {W/P} — multiple attackers pay multiple times")
    void multipleAttackersCostMultipleTax() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NornsAnnex());

        addCreatureReady(player2, new GrizzlyBears());

        addCreatureReady(player2, new GrizzlyBears());

        // Only 1 white mana — pays for one, life for the other
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackers(player2, List.of(0, 1));

        // 1 white mana spent + 2 life for the second attacker
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Player can choose not to attack when Norn's Annex is on the battlefield")
    void canDeclareNoAttackers() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NornsAnnex());

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Norn's Annex does not tax its controller's attackers")
    void doesNotTaxController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player 1 controls Norn's Annex and a creature
        harness.addToBattlefield(player1, new NornsAnnex());

        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        // No life loss, no mana cost for controller
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Must-attack creatures are not forced to attack when Norn's Annex imposes a tax")
    void mustAttackExemptionWithPhyrexianTax() {
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NornsAnnex());

        // Player 2 has a must-attack creature (Bloodrock Cyclops)
        Permanent cycloPerm = addCreatureReady(player2, new BloodrockCyclops());

        // Declaring no attackers should succeed (tax exempts must-attack)
        declareAttackers(player2, List.of());

        assertThat(cycloPerm.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Three attackers with no white mana costs 6 life total")
    void threeAttackersPaySixLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NornsAnnex());

        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }

        declareAttackers(player2, List.of(0, 1, 2));

        // 3 * 2 life = 6 life total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Multiple Annexes each require payment for the same attacker")
    void multipleAnnexesStackTheirCosts() {
        harness.addToBattlefield(player1, new NornsAnnex());
        harness.addToBattlefield(player1, new NornsAnnex());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Unaffordable combined payment spends neither mana nor life")
    void unaffordableCombinedPaymentIsAtomic() {
        harness.addToBattlefield(player1, new NornsAnnex());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setLife(player2, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertLife(player2, 1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.isAttacking()).isFalse();
        assertThat(second.isAttacking()).isFalse();
    }

    @Test
    @CardUsed(KarnLiberated.class)
    @DisplayName("Attacking an Annex controller's planeswalker requires payment")
    void planeswalkerAttackRequiresPayment() {
        harness.addToBattlefield(player1, new NornsAnnex());
        Permanent karn = harness.addToBattlefieldAndReturn(player1, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, karn.getId()));

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed(InvasionOfZendikar.class)
    @DisplayName("Annex does not tax an attack on a battle its controller protects")
    void battleAttackDoesNotRequirePayment() {
        harness.addToBattlefield(player1, new NornsAnnex());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(1), Map.of(1, battle.getId()));

        harness.assertLife(player2, 20);
    }
}
