package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeacekeeperTest extends BaseCardTest {

    private boolean controlsPeacekeeper(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Peacekeeper"));
    }

    private Permanent addReadyBears(Player player) {
        harness.addToBattlefield(player, new GrizzlyBears());
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        Permanent bears = battlefield.get(battlefield.size() - 1);
        bears.setSummoningSick(false);
        return bears;
    }

    @Test
    @DisplayName("Paying {1}{W} during your upkeep keeps Peacekeeper")
    void payingKeepsIt() {
        harness.addToBattlefield(player1, new Peacekeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsPeacekeeper(player1)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the payment sacrifices Peacekeeper")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new Peacekeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(controlsPeacekeeper(player1)).isFalse();
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Peacekeeper")
    void notEnoughManaSacrifices() {
        harness.addToBattlefield(player1, new Peacekeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(controlsPeacekeeper(player1)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Peacekeeper());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(controlsPeacekeeper(player1)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Your creatures can't attack while Peacekeeper is on the battlefield")
    void controllerCreaturesCantAttack() {
        harness.addToBattlefield(player1, new Peacekeeper());
        Permanent bears = addReadyBears(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The opponent's creatures can't attack either")
    void opponentCreaturesCantAttack() {
        harness.addToBattlefield(player1, new Peacekeeper());
        Permanent bears = addReadyBears(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures can attack again once Peacekeeper leaves the battlefield")
    void restrictionLiftsWhenPeacekeeperLeaves() {
        harness.addToBattlefield(player1, new Peacekeeper());
        Permanent bears = addReadyBears(player1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Peacekeeper"));

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        gs.declareAttackers(gd, player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
