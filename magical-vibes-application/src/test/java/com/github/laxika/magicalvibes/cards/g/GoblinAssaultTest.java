package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinAssaultTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    // ===== Upkeep token =====

    @Test
    @DisplayName("Creates a 1/1 red Goblin token with haste during controller's upkeep")
    void createsHastyGoblinDuringUpkeep() {
        harness.addToBattlefield(player1, new GoblinAssault());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent goblin = tokens.getFirst();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not create a token during an opponent's upkeep (\"your upkeep\" only)")
    void noTokenDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new GoblinAssault());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve any triggers

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== Static "Goblin creatures attack each combat if able" =====

    @Test
    @DisplayName("A Goblin the controller controls must attack while Goblin Assault is out")
    void controllersGoblinMustAttack() {
        harness.addToBattlefield(player1, new GoblinAssault());

        Permanent piker = new Permanent(new GoblinPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piker);

        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A non-Goblin creature is not forced to attack by Goblin Assault")
    void nonGoblinNotForced() {
        harness.addToBattlefield(player1, new GoblinAssault());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        beginDeclareAttackers(player1);

        // Grizzly Bears is not a Goblin, so Goblin Assault imposes no must-attack requirement.
        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Goblin Assault forces an opponent's Goblin to attack on the opponent's turn")
    void opponentsGoblinMustAttack() {
        harness.addToBattlefield(player1, new GoblinAssault());

        Permanent piker = new Permanent(new GoblinPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(piker);

        beginDeclareAttackers(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Without Goblin Assault, a Goblin is free to stay back")
    void goblinNotForcedWithoutAssault() {
        Permanent piker = new Permanent(new GoblinPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piker);

        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(piker.isAttacking()).isFalse();
    }
}
