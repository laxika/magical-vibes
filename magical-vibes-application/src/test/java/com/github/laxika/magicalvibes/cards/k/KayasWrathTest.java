package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KayasWrathTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Destroys all creatures and gains life only for the controller's destroyed creatures")
    void destroysAllCreaturesAndCountsOnlyOwnDestroyedCreatures() {
        Permanent ownCreature1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownCreature2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KayasWrath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ownCreature1.getId()) || p.getId().equals(ownCreature2.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 2);
    }

    @Test
    @DisplayName("Indestructible creatures are not destroyed and do not count toward life gained")
    void indestructibleCreaturesAreNotCounted() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent indestructible = harness.addToBattlefieldAndReturn(player1, new ManorGargoyle());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KayasWrath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ownCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(indestructible.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 1);
    }

    @Test
    @DisplayName("Gains no life when no controlled creature is destroyed")
    void gainsNoLifeWhenNoControlledCreatureIsDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new KayasWrath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
    }
}
