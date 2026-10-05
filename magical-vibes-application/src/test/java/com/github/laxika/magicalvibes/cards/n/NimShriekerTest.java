package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimShrieker.class, Ornithopter.class, GreatFurnace.class})
class NimShriekerTest extends BaseCardTest {

    @Test
    @DisplayName("Counts artifact lands once and does not boost other creatures")
    void countsArtifactLandsWithoutBoostingOtherCreatures() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimShrieker());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GreatFurnace());
        harness.addToBattlefield(player2, new GreatFurnace());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, thopter)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifacts outside the battlefield do not contribute to the bonus")
    void ignoresArtifactsOutsideTheBattlefield() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimShrieker());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setExile(player1, List.of(new Ornithopter()));

        assertThat(gqs.getEffectivePower(gd, nim)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+0 for each artifact controlled by its controller")
    void getsPowerForControlledArtifacts() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimShrieker());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus updates as a controlled artifact enters and leaves")
    void bonusTracksControlledArtifactsDynamically() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimShrieker());

        assertThat(gqs.getEffectivePower(gd, nim)).isZero();

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(1);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().tryDestroyPermanent(gd, artifact));

        assertThat(gqs.getEffectivePower(gd, nim)).isZero();
    }
}
