package com.github.laxika.magicalvibes.cards.m;

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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagistratesVetoTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures cannot block")
    void whiteCreaturesCannotBlock() {
        assertCannotBlock(CardColor.WHITE);
    }

    @Test
    @DisplayName("Blue creatures cannot block")
    void blueCreaturesCannotBlock() {
        assertCannotBlock(CardColor.BLUE);
    }

    @Test
    @DisplayName("Creatures that are neither white nor blue can block")
    void otherColorsCanBlock() {
        harness.addToBattlefield(player1, new MagistratesVeto());
        Permanent attacker = addReadyCreature(player1, CardColor.RED);
        attacker.setAttacking(true);
        addReadyCreature(player2, CardColor.GREEN);

        beginBlock();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));
    }

    private void assertCannotBlock(CardColor blockerColor) {
        harness.addToBattlefield(player1, new MagistratesVeto());
        Permanent attacker = addReadyCreature(player1, CardColor.RED);
        attacker.setAttacking(true);
        addReadyCreature(player2, blockerColor);

        beginBlock();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, CardColor color) {
        Card card = new Card();
        card.setName("Test " + color + " Creature");
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setColors(List.of(color));
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void beginBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
