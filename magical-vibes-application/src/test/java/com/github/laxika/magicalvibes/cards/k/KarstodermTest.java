package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
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
        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(karstoderm.getEffectivePower()).isEqualTo(4);
        assertThat(karstoderm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A nonartifact permanent does not remove a counter")
    void nonartifactDoesNotRemoveCounter() {
        Permanent karstoderm = castKarstoderm();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(karstoderm.getEffectivePower()).isEqualTo(5);
        assertThat(karstoderm.getEffectiveToughness()).isEqualTo(5);
    }

    private Permanent castKarstoderm() {
        harness.setHand(player1, List.of(new Karstoderm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Karstoderm");
    }
}
