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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(1);

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Flying creature mode only allows flying creatures")
    void flyingCreatureMode() {
        harness.addToBattlefield(player2, new WindDrake());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(2);

        harness.assertInGraveyard(player2, "Wind Drake");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(groundCreature);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each mode resolves when the opponent has no eligible permanent")
    void noEligiblePermanent(int mode) {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(mode);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pick Your Poison");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each mode leaves the caster's eligible permanents untouched")
    void casterDoesNotSacrifice(int mode) {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(mode);

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        if (mode == 1) {
            harness.assertInGraveyard(player2, "Glorious Anthem");
            harness.assertOnBattlefield(player2, "Ornithopter");
        } else {
            harness.assertInGraveyard(player2, "Ornithopter");
            harness.assertOnBattlefield(player2, "Glorious Anthem");
        }
    }

    @Test
    @DisplayName("The opponent chooses exactly one of multiple enchantments")
    void opponentChoosesEnchantment() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The opponent chooses a flying creature while ground creatures remain")
    void opponentChoosesFlyingCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(2);
        harness.handleMultiplePermanentsChosen(player2, List.of(thopter.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyInAnyOrder(drake, bears);
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new PickYourPoison()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalSorcery(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }
}
