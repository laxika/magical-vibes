package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlastFurnaceHellkite.class, DarksteelRelic.class, GrizzlyBears.class})
class BlastFurnaceHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures attacking an opponent have double strike")
    void attackingCreaturesHaveDoubleStrike() {
        Permanent hellkite = addCreatureReady(player1, new BlastFurnaceHellkite());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattackingBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattackingBears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creatures attacking the Hellkite's controller do not gain double strike")
    void creaturesAttackingControllerDoNotHaveDoubleStrike() {
        addCreatureReady(player1, new BlastFurnaceHellkite());
        Permanent attackingBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gqs.hasKeyword(gd, attackingBears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Artifact offering sacrifices an artifact and casts the Hellkite")
    void artifactOfferingCastsHellkite() {
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new BlastFurnaceHellkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(relic.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blast-Furnace Hellkite");
        harness.assertNotOnBattlefield(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Artifact offering cannot sacrifice a nonartifact")
    void artifactOfferingRequiresArtifact() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlastFurnaceHellkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
