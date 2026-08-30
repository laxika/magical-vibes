package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Flicker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeTreeScribe.class, GrizzlyBears.class, Flicker.class})
class ThreeTreeScribeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a creature you control when another creature leaves without dying")
    void putsCounterWhenAllyLeavesWithoutDying() {
        addCreatureReady(player1, new ThreeTreeScribe());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, leaving.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when Three Tree Scribe itself leaves without dying")
    void triggersWhenItselfLeavesWithoutDying() {
        Permanent scribe = addCreatureReady(player1, new ThreeTreeScribe());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, scribe.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature dies")
    void doesNotTriggerWhenCreatureDies() {
        addCreatureReady(player1, new ThreeTreeScribe());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent dying = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature leaves without dying")
    void doesNotTriggerWhenOpponentCreatureLeavesWithoutDying() {
        addCreatureReady(player1, new ThreeTreeScribe());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent leaving = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, leaving));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
