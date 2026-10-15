package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrosanGrip;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hivestone.class, GrizzlyBears.class, KrosanGrip.class, MarchOfTheMachines.class})
class HivestoneTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control are Slivers in addition to their other types")
    void grantsSliverToOwnCreatures() {
        harness.addToBattlefield(player1, new Hivestone());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.effectiveCreatureSubtypes(gd, ownBear))
                .contains(CardSubtype.BEAR, CardSubtype.SLIVER);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingBear))
                .containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The subtype grant applies to creatures entering after Hivestone")
    void appliesToCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new Hivestone());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.effectiveCreatureSubtypes(gd, bear))
                .contains(CardSubtype.BEAR, CardSubtype.SLIVER);
    }

    @Test
    @DisplayName("Hivestone grants itself Sliver when it becomes a creature")
    void animatedHivestoneIsASliver() {
        Permanent hivestone = harness.addToBattlefieldAndReturn(player1, new Hivestone());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        assertThat(gqs.isCreature(gd, hivestone)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hivestone)).contains(CardSubtype.SLIVER);
    }

    @Test
    @DisplayName("Hivestone is a Sliver when it enters under an existing animation effect")
    void hivestoneEnteringAsACreatureIsASliver() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent hivestone = harness.enterBattlefieldAndReturn(player1, new Hivestone());

        assertThat(gqs.isCreature(gd, hivestone)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hivestone)).contains(CardSubtype.SLIVER);
    }

    @Test
    @DisplayName("Creatures lose the granted subtype when Hivestone is destroyed")
    void grantEndsWhenHivestoneLeaves() {
        Permanent hivestone = harness.addToBattlefieldAndReturn(player1, new Hivestone());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear)).contains(CardSubtype.SLIVER);

        harness.setHand(player1, List.of(new KrosanGrip()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, hivestone.getId());

        harness.assertInGraveyard(player1, "Hivestone");
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Hivestone does not grant Sliver to creature cards outside the battlefield")
    void doesNotAffectCardsInOtherZones() {
        harness.addToBattlefield(player1, new Hivestone());
        GrizzlyBears handBear = new GrizzlyBears();
        GrizzlyBears graveyardBear = new GrizzlyBears();
        GrizzlyBears libraryBear = new GrizzlyBears();
        GrizzlyBears exiledBear = new GrizzlyBears();
        harness.setHand(player1, List.of(handBear));
        harness.setGraveyard(player1, List.of(graveyardBear));
        harness.setLibrary(player1, List.of(libraryBear));
        harness.setExile(player1, List.of(exiledBear));

        for (GrizzlyBears bear : List.of(handBear, graveyardBear, libraryBear, exiledBear)) {
            assertThat(gqs.getCardSubtypes(bear, gd, player1.getId())).doesNotContain(CardSubtype.SLIVER);
        }
    }
}
