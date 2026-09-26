package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimGrotesque.class, ConjurersBauble.class})
class NimGrotesqueTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each artifact you control")
    void scalesWithControlledArtifacts() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());
        harness.addToBattlefield(player1, new ConjurersBauble());
        harness.addToBattlefield(player1, new ConjurersBauble());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(6);
    }

    @Test
    @DisplayName("Has no bonus with no artifacts")
    void hasNoBonusWithoutArtifacts() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nim)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts only artifacts controlled by its controller")
    void opponentArtifactsDoNotCount() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());
        harness.addToBattlefield(player2, new ConjurersBauble());
        harness.addToBattlefield(player2, new ConjurersBauble());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus updates when a controlled artifact leaves")
    void updatesWhenArtifactLeaves() {
        Permanent nim = harness.addToBattlefieldAndReturn(player1, new NimGrotesque());
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());
        harness.addToBattlefield(player1, new ConjurersBauble());

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(5);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bauble));

        assertThat(gqs.getEffectivePower(gd, nim)).isEqualTo(4);
    }
}
