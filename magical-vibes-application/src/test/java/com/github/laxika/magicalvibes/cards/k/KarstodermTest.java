package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Karstoderm.class, DarksteelBrute.class, CrazedGoblin.class})
class KarstodermTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five +1/+1 counters")
    void entersWithFiveCounters() {
        Permanent karstoderm = castKarstoderm();

        assertThat(karstoderm.getEffectivePower()).isEqualTo(5);
        assertThat(karstoderm.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Loses a +1/+1 counter when an artifact enters under your control")
    void losesCounterForOwnArtifact() {
        Permanent karstoderm = castKarstoderm();

        harness.castFromHand(player1, new DarksteelBrute(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(karstoderm.getEffectivePower()).isEqualTo(4);
        assertThat(karstoderm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses a +1/+1 counter when an artifact enters under an opponent's control")
    void losesCounterForOpponentsArtifact() {
        Permanent karstoderm = castKarstoderm();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new DarksteelBrute(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(karstoderm.getEffectivePower()).isEqualTo(4);
        assertThat(karstoderm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A nonartifact permanent does not remove a counter")
    void nonartifactDoesNotRemoveCounter() {
        Permanent karstoderm = castKarstoderm();

        harness.castFromHand(player1, new CrazedGoblin(), "{R}");
        harness.passBothPriorities();

        assertThat(karstoderm.getEffectivePower()).isEqualTo(5);
        assertThat(karstoderm.getEffectiveToughness()).isEqualTo(5);
    }

    private Permanent castKarstoderm() {
        harness.castFromHand(player1, new Karstoderm(), "{2}{G}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Karstoderm");
    }
}
