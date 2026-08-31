package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
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

@CardUsed({GlyphOfDestruction.class, WallOfWood.class, GrizzlyBears.class, Shock.class})
class GlyphOfDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and protects a blocking Wall, then destroys it at the next end step")
    void boostsProtectsAndDestroysBlockingWall() {
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player2, List.of(new GlyphOfDestruction()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getEffectivePower()).isEqualTo(10);
        assertThat(wall.getEffectiveToughness()).isEqualTo(3);

        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isEqualTo(0);
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player2, "Wall of Wood");
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
        Permanent nonblockingWall = addCreatureReady(player1, new WallOfWood());
        prepareSpellCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonblockingWall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking Wall you control");
    }

    private Permanent addBlockingWall(Player player) {
        Permanent wall = addCreatureReady(player, new WallOfWood());
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
