package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PickYourPoison.class, MindStone.class, Ornithopter.class, GloriousAnthem.class,
        WindDrake.class, GrizzlyBears.class})
class PickYourPoisonTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode makes each opponent choose an artifact to sacrifice")
    void artifactMode() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        cast(0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact);
    }

    @Test
    @DisplayName("Enchantment mode sacrifices an enchantment from each opponent")
    void enchantmentMode() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1);

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Flying creature mode only allows flying creatures")
    void flyingCreatureMode() {
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(2);

        harness.assertInGraveyard(player2, "Wind Drake");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(groundCreature);
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new PickYourPoison()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }
}
