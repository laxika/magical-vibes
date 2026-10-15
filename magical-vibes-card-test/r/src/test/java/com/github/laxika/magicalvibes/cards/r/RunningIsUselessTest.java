package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunningIsUseless.class, GrizzlyBears.class, LlanowarElves.class})
class RunningIsUselessTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys any number of creatures with different mana values")
    void destroysChosenCreaturesWithDistinctManaValues() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent unchosenBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveScheme();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                bears.getId(), elves.getId(), unchosenBears.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), elves.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(elves);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosenBears);
    }

    @Test
    @DisplayName("Rejects a selection containing creatures with the same mana value")
    void rejectsDuplicateManaValues() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveScheme();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different mana values");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void allowsEmptySelection() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveScheme();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    private void resolveScheme() {
        Card scheme = new RunningIsUseless();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
