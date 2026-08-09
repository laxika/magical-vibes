package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArcadesTest extends BaseCardTest {

    @Test
    @DisplayName("Defender creatures assign combat damage equal to toughness")
    void defenderCreaturesUseToughnessForCombatDamage() {
        harness.addToBattlefield(player1, new Arcades());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Defender creatures can attack")
    void defenderCreaturesCanAttack() {
        harness.addToBattlefield(player1, new Arcades());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Draws when a defender creature enters under its controller's control")
    void drawsWhenDefenderEnters() {
        harness.addToBattlefield(player1, new Arcades());
        harness.setHand(player1, List.of(new WallOfVines()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when a non-defender creature enters")
    void doesNotDrawWhenNonDefenderEnters() {
        harness.addToBattlefield(player1, new Arcades());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new WallOfVines()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
