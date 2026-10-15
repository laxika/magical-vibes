package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasaltRavager.class, GrizzlyBears.class, MaskedVandal.class, FrostBite.class})
class BasaltRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the greatest shared creature type count")
    void dealsDamageEqualToGreatestSharedCreatureTypeCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castBasaltRavager(player2.getId());

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("ETB counts only creatures controlled by its controller")
    void countsOnlyControllerCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBasaltRavager(player2.getId());

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB can damage a creature")
    void damagesCreatureTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        java.util.UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castBasaltRavager(targetId);

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A lone Ravager counts itself once despite having two creature types")
    void countsItselfOnce() {
        castBasaltRavager(player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Creatures sharing both Giant and Wizard are not counted twice")
    void doesNotSumDifferentCreatureTypeCounts() {
        harness.addToBattlefield(player1, new BasaltRavager());
        castBasaltRavager(player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Changelings contribute to the greatest shared creature type count")
    void includesChangelings() {
        harness.addToBattlefield(player1, new MaskedVandal());
        harness.addToBattlefield(player1, new MaskedVandal());
        castBasaltRavager(player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Damage uses the remaining creatures when Ravager dies before resolution")
    void usesCurrentCreaturesAfterSourceDies() {
        harness.addToBattlefield(player1, new MaskedVandal());
        castBasaltRavager(player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Basalt Ravager"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Basalt Ravager");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger deals no damage when no controlled creatures remain")
    void dealsZeroWithNoRemainingCreatures() {
        castBasaltRavager(player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Basalt Ravager"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Basalt Ravager");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castBasaltRavager(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BasaltRavager()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, targetId);
    }
}
