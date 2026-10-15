package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CullingDrone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathwayArrows.class, CullingDrone.class, GrizzlyBears.class, Forest.class})
class PathwayArrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature damages and taps a colorless creature")
    void damagesAndTapsColorlessCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent arrows = addPathwayArrowsReady(player1);
        arrows.setAttachedTo(equippedCreature.getId());
        Permanent target = addCreatureReady(player2, new CullingDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(equippedCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature damages but does not tap a colored creature")
    void damagesButDoesNotTapColoredCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent arrows = addPathwayArrowsReady(player1);
        arrows.setAttachedTo(equippedCreature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent arrows = addPathwayArrowsReady(player1);
        arrows.setAttachedTo(equippedCreature.getId());
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addPathwayArrowsReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PathwayArrows());
    }
}
