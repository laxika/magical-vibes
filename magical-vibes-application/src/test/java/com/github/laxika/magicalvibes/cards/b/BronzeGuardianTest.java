package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzeGuardian.class, FountainOfYouth.class, Shock.class})
class BronzeGuardianTest extends BaseCardTest {

    @Test
    void powerEqualsArtifactsYouControlAndOtherArtifactsGainWard() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new BronzeGuardian());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.WARD)).isTrue();

        harness.addToBattlefield(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(3);
    }

    @Test
    void wardCountersSpellTargetingOtherArtifact() {
        harness.addToBattlefield(player1, new BronzeGuardian());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }
}
