package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FumikoTheLowblood.class, GnarledMass.class})
class FumikoTheLowbloodTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido X scales to the number of attacking creatures when Fumiko becomes blocked")
    void bushidoScalesWithAttackersWhenBlocked() {
        Permanent fumiko = addCreatureReady(player1, new FumikoTheLowblood());
        Permanent ally = addCreatureReady(player1, new GnarledMass());
        fumiko.setAttacking(true);
        ally.setAttacking(true);
        addCreatureReady(player2, new GnarledMass());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(fumiko.getPowerModifier()).isEqualTo(2);
        assertThat(fumiko.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido X scales to the number of attacking creatures when Fumiko blocks")
    void bushidoScalesWithAttackersWhenBlocking() {
        Permanent attacker1 = addCreatureReady(player1, new GnarledMass());
        Permanent attacker2 = addCreatureReady(player1, new GnarledMass());
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);
        Permanent fumiko = addCreatureReady(player2, new FumikoTheLowblood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(fumiko.getPowerModifier()).isEqualTo(2);
        assertThat(fumiko.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido X is calculated from the attackers when its trigger resolves")
    void bushidoUsesAttackerCountAtResolution() {
        Permanent fumiko = addCreatureReady(player1, new FumikoTheLowblood());
        Permanent ally = addCreatureReady(player1, new GnarledMass());
        fumiko.setAttacking(true);
        ally.setAttacking(true);
        addCreatureReady(player2, new GnarledMass());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        ally.setAttacking(false);
        harness.passBothPriorities();

        assertThat(fumiko.getPowerModifier()).isEqualTo(1);
        assertThat(fumiko.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unblocked Fumiko gets no Bushido bonus")
    void unblockedGetsNoBushido() {
        Permanent fumiko = addCreatureReady(player1, new FumikoTheLowblood());
        fumiko.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(fumiko.getPowerModifier()).isZero();
        assertThat(fumiko.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's creature must attack while Fumiko is on the battlefield")
    void opponentCreaturesMustAttack() {
        harness.addToBattlefield(player1, new FumikoTheLowblood());

        Permanent bears = addCreatureReady(player2, new GnarledMass());

        beginDeclareAttackers(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Tapped or summoning-sick opposing creatures are exempt from the attack requirement")
    void opposingCreaturesUnableToAttackAreExempt() {
        harness.addToBattlefield(player1, new FumikoTheLowblood());

        Permanent tappedCreature = addCreatureReady(player2, new GnarledMass());
        tappedCreature.tap();
        Permanent summoningSickCreature = addCreatureReady(player2, new GnarledMass());
        summoningSickCreature.setSummoningSick(true);

        beginDeclareAttackers(player2);

        assertThatCode(() -> gs.declareAttackers(gd, player2, List.of()))
                .doesNotThrowAnyException();
        assertThat(tappedCreature.isAttacking()).isFalse();
        assertThat(summoningSickCreature.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Fumiko's controller's creatures are not forced to attack by her static ability")
    void ownCreaturesNotForced() {
        harness.addToBattlefield(player1, new FumikoTheLowblood());
        Permanent bears = addCreatureReady(player1, new GnarledMass());

        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

}
