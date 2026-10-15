package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TheRoyalScions;
import com.github.laxika.magicalvibes.cards.t.TuinvaleTreefolk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchonOfAbsolution.class, TuinvaleTreefolk.class, TheRoyalScions.class})
class ArchonOfAbsolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Archon of Absolution has protection from white")
    void hasProtectionFromWhite() {
        Permanent archon = harness.addToBattlefieldAndReturn(player1, new ArchonOfAbsolution());

        assertThat(gqs.hasProtectionFrom(gd, archon, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, archon, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Opponent pays {1} for each creature attacking the controller")
    void opponentPaysOnePerAttacker() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        addCreatureReady(player2, new TuinvaleTreefolk());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent cannot attack without paying the tax")
    void opponentCannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        addCreatureReady(player2, new TuinvaleTreefolk());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("The tax also applies to attacks against the controller's planeswalker")
    void planeswalkerAttackIsTaxed() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new TheRoyalScions());
        addCreatureReady(player2, new TuinvaleTreefolk());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    @Test
    @DisplayName("Each of two attackers requires its own payment")
    void twoAttackersRequireTwoMana() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        Permanent first = addCreatureReady(player2, new TuinvaleTreefolk());
        Permanent second = addCreatureReady(player2, new TuinvaleTreefolk());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0, 1)));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Two Archons charge twice for each attacker")
    void multipleArchonsStackTheirTaxes() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        addCreatureReady(player2, new TuinvaleTreefolk());
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The controller's own attackers are not taxed")
    void controllerCanAttackWithoutPaying() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        Permanent attacker = addCreatureReady(player1, new TuinvaleTreefolk());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Paying the tax allows an attack against a planeswalker")
    void planeswalkerAttackCanBePaidFor() {
        harness.addToBattlefield(player1, new ArchonOfAbsolution());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new TheRoyalScions());
        addCreatureReady(player2, new TuinvaleTreefolk());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId())));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A white flying creature cannot block the Archon")
    void whiteFlyerCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ArchonOfAbsolution());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ArchonOfAbsolution());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block the Archon")
    void groundCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new ArchonOfAbsolution());
        attacker.setAttacking(true);
        addCreatureReady(player2, new TuinvaleTreefolk());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
