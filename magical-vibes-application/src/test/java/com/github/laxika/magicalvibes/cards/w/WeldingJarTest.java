package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeldingJarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Welding Jar regenerates target artifact")
    void sacrificesJarAndRegeneratesTargetArtifact() {
        harness.addToBattlefield(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Welding Jar");
        harness.assertInGraveyard(player1, "Welding Jar");

        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Fountain of Youth").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Welding Jar cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        harness.addToBattlefield(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
