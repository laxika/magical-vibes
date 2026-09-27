package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightRampager.class, Murder.class, JaceBeleren.class})
class KnightRampagerTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Knight Rampager must attack the randomly chosen opponent")
    void mustAttackRandomlyChosenOpponent() {
        Permanent knight = addReadyKnight(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(knight.isMustAttackThisCombat()).isTrue();
        assertThat(knight.getMustAttackTargetId()).isEqualTo(player2.getId());

        beginDeclareAttackers(player1);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("When Knight Rampager dies, it deals 4 damage to an opponent")
    void deathTriggerDamagesOpponent() {
        Permanent knight = addReadyKnight(player1);
        harness.setLife(player2, 20);

        killKnight(knight);

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The death trigger damages the opponent rather than choosing their planeswalker")
    void deathTriggerDoesNotTargetOpponentPlaneswalker() {
        Permanent knight = addReadyKnight(player1);
        harness.addToBattlefield(player2, new JaceBeleren());
        harness.setLife(player2, 20);

        killKnight(knight);

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyKnight(Player player) {
        Permanent knight = new Permanent(new KnightRampager());
        knight.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(knight);
        return knight;
    }

    private void killKnight(Permanent knight) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
