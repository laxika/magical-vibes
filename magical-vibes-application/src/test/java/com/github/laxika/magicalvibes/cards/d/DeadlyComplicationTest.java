package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DeadlyComplication.class, GrizzlyBears.class})
class DeadlyComplicationTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys target creature")
    void destroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The counter mode puts a counter on a suspected creature and may clear suspect")
    void putsCounterAndClearsSuspect() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSuspected(true);

        cast(new int[]{1}, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("Choosing both modes resolves each mode against its target")
    void resolvesBothModes() {
        Permanent creatureToDestroy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent suspectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        suspectedCreature.setSuspected(true);

        cast(new int[]{0, 1}, List.of(creatureToDestroy.getId(), suspectedCreature.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(suspectedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(suspectedCreature.isSuspected()).isFalse();
    }

    private void cast(int[] modes, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new DeadlyComplication()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targets, null);
        harness.passBothPriorities();
    }
}
