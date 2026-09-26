package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFrightfulFour.class, GrizzlyBears.class, MindStone.class})
class TheFrightfulFourTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent loses life equal to their first noncreature spell's mana value each turn")
    void punishesFirstNoncreatureSpellEachTurn() {
        harness.addToBattlefield(player1, new TheFrightfulFour());
        harness.setHand(player2, List.of(new MindStone(), new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        prepareCast(player2);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castArtifact(player2, 0);
            while (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }
        });
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        prepareCast(player2);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creature spells do not use up the opponent's first noncreature spell")
    void ignoresCreatureSpells() {
        harness.addToBattlefield(player1, new TheFrightfulFour());
        harness.setHand(player2, List.of(new GrizzlyBears(), new MindStone()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareCast(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The controller's noncreature spells do not trigger the ability")
    void ignoresControllerSpells() {
        harness.addToBattlefield(player1, new TheFrightfulFour());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareCast(player1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The first noncreature spell count resets on the opponent's next turn")
    void resetsEachTurn() {
        harness.addToBattlefield(player1, new TheFrightfulFour());
        castMindStone(player2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        endTurn();
        endTurn();

        castMindStone(player2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void castMindStone(Player player) {
        harness.setHand(player, List.of(new MindStone()));
        harness.addMana(player, ManaColor.COLORLESS, 2);
        prepareCast(player);
        harness.castArtifact(player, 0);
        harness.passBothPriorities();
    }

    private void prepareCast(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void endTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
