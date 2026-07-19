package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourtHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("With no other artifact, is a plain 1/1")
    void withoutAnotherArtifact() {
        Permanent homunculus = addCreatureReady(player1, new CourtHomunculus());

        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, homunculus)).isEqualTo(1);
    }

    @Test
    @DisplayName("While you control another artifact, gets +1/+1")
    void withAnotherArtifact() {
        Permanent homunculus = addCreatureReady(player1, new CourtHomunculus());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, homunculus)).isEqualTo(2);
    }

    @Test
    @DisplayName("An artifact an opponent controls does not grant the boost")
    void opponentArtifactDoesNotCount() {
        Permanent homunculus = addCreatureReady(player1, new CourtHomunculus());
        harness.addToBattlefield(player2, new AccordersShield());

        assertThat(gqs.getEffectivePower(gd, homunculus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, homunculus)).isEqualTo(1);
    }
}
