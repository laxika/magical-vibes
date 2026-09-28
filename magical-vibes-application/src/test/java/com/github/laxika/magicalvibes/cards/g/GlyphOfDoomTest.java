package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlyphOfDoom.class, WallOfEarth.class, DurkwoodBoars.class})
class GlyphOfDoomTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures blocked by the targeted Wall at the next end of combat")
    void destroysCreaturesBlockedByTargetedWall() {
        Permanent wall = addCreatureReady(player2, new WallOfEarth());
        Permanent otherBlocker = addCreatureReady(player2, new WallOfEarth());
        Permanent affectedAttacker = addCreatureReady(player1, new DurkwoodBoars());
        Permanent unaffectedAttacker = addCreatureReady(player1, new DurkwoodBoars());
        affectedAttacker.setAttacking(true);
        unaffectedAttacker.setAttacking(true);

        castGlyph(wall);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(affectedAttacker, unaffectedAttacker);

        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(unaffectedAttacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(wall, otherBlocker);
        harness.assertInGraveyard(player1, "Durkwood Boars");
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWallCreature() {
        Permanent bears = addCreatureReady(player2, new DurkwoodBoars());

        harness.setHand(player1, List.of(new GlyphOfDoom()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGlyph(Permanent wall) {
        harness.setHand(player1, List.of(new GlyphOfDoom()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();
    }

    private void advanceThroughEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
