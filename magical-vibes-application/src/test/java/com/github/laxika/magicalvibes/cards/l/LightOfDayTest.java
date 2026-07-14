package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LightOfDayTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature cannot attack while Light of Day is on the battlefield")
    void blackCreatureCannotAttack() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent black = addReadyCreature(player1, CardColor.BLACK);

        beginAttack(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(black);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(idx)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black creature attacks normally while Light of Day is on the battlefield")
    void nonBlackCreatureCanAttack() {
        harness.addToBattlefield(player1, new LightOfDay());
        harness.setLife(player2, 20);
        Permanent white = addReadyCreature(player1, CardColor.WHITE);

        beginAttack(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(white);
        gs.declareAttackers(gd, player1, List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Black creature cannot block while Light of Day is on the battlefield")
    void blackCreatureCannotBlock() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent attacker = addReadyCreature(player1, CardColor.WHITE);
        attacker.setAttacking(true);
        addReadyCreature(player2, CardColor.BLACK);

        beginBlock();

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-black creature blocks normally while Light of Day is on the battlefield")
    void nonBlackCreatureCanBlock() {
        harness.addToBattlefield(player1, new LightOfDay());
        Permanent attacker = addReadyCreature(player1, CardColor.WHITE);
        attacker.setAttacking(true);
        addReadyCreature(player2, CardColor.GREEN);

        beginBlock();
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIdx)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Black creature can attack again after Light of Day leaves the battlefield")
    void restrictionLiftsWhenLightOfDayLeaves() {
        Permanent lightOfDay = new Permanent(new LightOfDay());
        gd.playerBattlefields.get(player1.getId()).add(lightOfDay);
        harness.setLife(player2, 20);
        Permanent black = addReadyCreature(player1, CardColor.BLACK);

        gd.playerBattlefields.get(player1.getId()).remove(lightOfDay);

        beginAttack(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(black);
        gs.declareAttackers(gd, player1, List.of(idx));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCreature(Player player, CardColor color) {
        Card card = new Card();
        card.setName("Test " + color + " Creature");
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setColors(List.of(color));
        card.setPower(2);
        card.setToughness(2);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void beginBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
