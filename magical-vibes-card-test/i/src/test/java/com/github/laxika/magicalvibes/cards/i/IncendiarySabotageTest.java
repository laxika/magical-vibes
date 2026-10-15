package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncendiarySabotage.class, CrawWurm.class, GrizzlyBears.class,
        PropheticPrism.class, PrakhataPillarBug.class})
class IncendiarySabotageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact deals 3 damage to each creature")
    void sacrificesArtifactAndDamagesEachCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.setHand(player1, List.of(new IncendiarySabotage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Incendiary Sabotage");
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new IncendiarySabotage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Artifact creature is sacrificed before the spell deals lethal damage to other creatures")
    void sacrificesArtifactCreatureBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PrakhataPillarBug());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PrakhataPillarBug());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new PrakhataPillarBug());
        Permanent noncreatureArtifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new IncendiarySabotage()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice).contains(ownCreature);
        harness.assertInGraveyard(player1, "Prakhata Pillar-Bug");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature).contains(noncreatureArtifact);
        harness.assertInGraveyard(player2, "Prakhata Pillar-Bug");
        assertThat(noncreatureArtifact.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's artifact")
    void cannotSacrificeOpponentsArtifact() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new IncendiarySabotage()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opposingArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingArtifact);
    }

    @Test
    @DisplayName("Sacrificing an artifact is mandatory even when no creatures are on the battlefield")
    void cannotOmitSacrifice() {
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new IncendiarySabotage()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Prophetic Prism");
    }
}
