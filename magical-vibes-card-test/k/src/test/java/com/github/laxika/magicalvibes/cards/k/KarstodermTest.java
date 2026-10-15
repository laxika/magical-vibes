package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
import com.github.laxika.magicalvibes.model.CounterType;
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
        resolveAllTriggers();

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
        resolveAllTriggers();

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

    @Test
    @DisplayName("Removes a counter only when the artifact entry trigger resolves")
    void counterRemovalUsesTheStack() {
        Permanent karstoderm = castKarstoderm();

        harness.castFromHand(player1, new DarksteelBrute(), "{2}");
        assertThat(karstoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Darksteel Brute");
        assertThat(karstoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(karstoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dies after the fifth artifact entry removes its last counter")
    void diesWhenLastCounterIsRemoved() {
        Permanent karstoderm = castKarstoderm();

        for (int i = 0; i < 4; i++) {
            harness.castFromHand(player1, new DarksteelBrute(), "{2}");
            resolveAllTriggers();
            assertThat(karstoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4 - i);
            harness.assertOnBattlefield(player1, "Karstoderm");
        }

        harness.castFromHand(player1, new DarksteelBrute(), "{2}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Karstoderm");
        harness.assertInGraveyard(player1, "Karstoderm");
    }

    @Test
    @DisplayName("Each Karstoderm removes its own counter for an artifact entry")
    void multipleKarstodermsTriggerIndependently() {
        Permanent first = castKarstoderm();
        harness.castFromHand(player1, new Karstoderm(), "{2}{G}{G}");
        harness.passBothPriorities();
        Permanent second = findPermanents(player1, "Karstoderm").stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();

        harness.castFromHand(player1, new DarksteelBrute(), "{2}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private Permanent castKarstoderm() {
        harness.castFromHand(player1, new Karstoderm(), "{2}{G}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Karstoderm");
    }
}
