package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AzoriusLocket;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KayasWrath.class, GrizzlyBears.class, ManorGargoyle.class, AzoriusLocket.class, DrudgeSkeletons.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
    }

    @Test
    @DisplayName("Regenerated creatures survive and do not count toward life gained")
    void regeneratedCreaturesAreNotCounted() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new KayasWrath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        assertThat(skeletons.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, STARTING_LIFE + 1);
        harness.assertLife(player2, STARTING_LIFE);
    }

    @Test
    @DisplayName("Noncreature permanents survive and do not contribute to life gained")
    void noncreaturePermanentsAreUnaffected() {
        harness.addToBattlefield(player1, new AzoriusLocket());
        harness.addToBattlefield(player2, new AzoriusLocket());
        harness.setHand(player1, List.of(new KayasWrath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Azorius Locket");
        harness.assertOnBattlefield(player2, "Azorius Locket");
        harness.assertLife(player1, STARTING_LIFE);
        harness.assertLife(player2, STARTING_LIFE);
        harness.assertInGraveyard(player1, "Kaya's Wrath");
    }
}
