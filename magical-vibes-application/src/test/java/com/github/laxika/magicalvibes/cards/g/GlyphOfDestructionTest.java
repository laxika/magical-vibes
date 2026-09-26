package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrimsonKobolds;
import com.github.laxika.magicalvibes.cards.p.PsychicPurge;
import com.github.laxika.magicalvibes.cards.w.WallOfCaltrops;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlyphOfDestruction.class, WallOfCaltrops.class, CrimsonKobolds.class, PsychicPurge.class})
class GlyphOfDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and protects a blocking Wall, then destroys it at the next end step")
    void boostsProtectsAndDestroysBlockingWall() {
        Permanent wall = addCreatureReady(player2, new WallOfCaltrops());
        addCreatureReady(player1, new CrimsonKobolds());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player2, List.of(new GlyphOfDestruction()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getEffectivePower()).isEqualTo(12);
        assertThat(wall.getEffectiveToughness()).isEqualTo(1);

        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isEqualTo(0);
        harness.assertInGraveyard(player1, "Crimson Kobolds");

        harness.setHand(player1, List.of(new PsychicPurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(0);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player2, "Wall of Caltrops");
    }

    @Test
    @DisplayName("Ends the +10/+0 boost at end of combat")
    void boostExpiresAtEndOfCombat() {
        Permanent wall = addCreatureReady(player1, new WallOfCaltrops());
        addCreatureReady(player2, new CrimsonKobolds());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new GlyphOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getEffectivePower()).isEqualTo(12);

        resolveCombat(player2);

        assertThat(wall.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a Wall not controlled by the spell's controller")
    void cannotTargetOpponentWall() {
        Permanent ownWall = addBlockingWall(player1);
        Permanent opponentWall = addBlockingWall(player2);
        prepareSpellCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentWall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking Wall you control");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownWall);
    }

    @Test
    @DisplayName("Cannot target a Wall that is not blocking")
    void cannotTargetNonblockingWall() {
        addBlockingWall(player1);
        Permanent nonblockingWall = addCreatureReady(player1, new WallOfCaltrops());
        prepareSpellCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonblockingWall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking Wall you control");
    }

    @Test
    @DisplayName("Cannot target a blocking creature that is not a Wall")
    void cannotTargetBlockingNonWall() {
        Permanent nonWall = addCreatureReady(player1, new CrimsonKobolds());
        nonWall.setBlocking(true);
        prepareSpellCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonWall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking Wall you control");
    }

    private Permanent addBlockingWall(Player player) {
        Permanent wall = addCreatureReady(player, new WallOfCaltrops());
        wall.setBlocking(true);
        return wall;
    }

    private void prepareSpellCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GlyphOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
