package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinFangFoom.class, DarksteelMyr.class, LightningBolt.class, Mountain.class, StoneRain.class})
class FinFangFoomTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an instant targeting an artifact and gets two +1/+1 counters")
    void copiesSpellTargetingArtifactAndGetsCounters() {
        Permanent finFangFoom = harness.addToBattlefieldAndReturn(player1, new FinFangFoom());
        Permanent artifact = addReadyArtifact();
        castLightningBolt(artifact);

        resolveAllFoomTriggers();

        assertThat(finFangFoom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(artifact.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Copies an instant targeting a land and gets two +1/+1 counters")
    void copiesSpellTargetingLandAndGetsCounters() {
        Permanent finFangFoom = harness.addToBattlefieldAndReturn(player1, new FinFangFoom());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, land.getId());

        resolveAllFoomTriggers();

        assertThat(finFangFoom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gameLogContains("A copy of Stone Rain is created.")).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for an instant targeting a player")
    void doesNotTriggerForSpellTargetingPlayer() {
        Permanent finFangFoom = harness.addToBattlefieldAndReturn(player1, new FinFangFoom());
        castLightningBolt(player2.getId());

        resolveAllFoomTriggers();

        assertThat(finFangFoom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent addReadyArtifact() {
        return harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
    }

    private void castLightningBolt(Permanent target) {
        castLightningBolt(target.getId());
    }

    private void castLightningBolt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
    }

    private void resolveAllFoomTriggers() {
        while (!gd.stack.isEmpty()
                || gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, false);
            } else {
                harness.passBothPriorities();
            }
        }
    }
}
