package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaveElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Taps three target creatures without flying and sacrifices itself")
    void tapsThreeCreatures() {
        Permanent elemental = addReadyElemental(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent bearA = addCreatureReady(player2, new GrizzlyBears());
        Permanent bearB = addCreatureReady(player2, new GrizzlyBears());
        Permanent bearC = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(bearA.getId(), bearB.getId(), bearC.getId()));
        harness.passBothPriorities();

        assertThat(bearA.isTapped()).isTrue();
        assertThat(bearB.isTapped()).isTrue();
        assertThat(bearC.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elemental.getCard());
    }

    @Test
    @DisplayName("Up to three — a single target is legal")
    void tapsSingleCreature() {
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(angel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyElemental(Player player) {
        return addCreatureReady(player, new WaveElemental());
    }
}
