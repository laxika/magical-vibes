package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodcrazedHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic puts a counter on Bloodcrazed Hoplite and removes one from a target opponent creature")
    void heroicRemovesCounterFromTargetOpponentCreature() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, hoplite.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger Bloodcrazed Hoplite")
    void spellTargetingAnotherCreatureDoesNotTriggerHeroic() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's spell targeting Bloodcrazed Hoplite does not trigger heroic")
    void opponentSpellDoesNotTriggerHeroic() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, hoplite.getId());
        harness.passBothPriorities();

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
