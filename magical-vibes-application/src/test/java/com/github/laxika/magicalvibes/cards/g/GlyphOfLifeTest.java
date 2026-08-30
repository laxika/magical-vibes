package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
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

@CardUsed({GlyphOfLife.class, WallOfWood.class, GrizzlyBears.class, Shock.class})
class GlyphOfLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life when an attacking creature deals damage to the targeted Wall")
    void gainsLifeFromAttackingCreatureDamage() {
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        castGlyph(wall);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger from noncombat damage")
    void ignoresNoncombatDamage() {
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        castGlyph(wall);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The delayed trigger wears off at end of turn")
    void triggerWearsOffAtEndOfTurn() {
        Permanent wall = addCreatureReady(player2, new WallOfWood());
        addCreatureReady(player1, new GrizzlyBears());
        castGlyph(wall);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWallCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlyphOfLife()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGlyph(Permanent wall) {
        harness.setHand(player1, List.of(new GlyphOfLife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();
    }
}
