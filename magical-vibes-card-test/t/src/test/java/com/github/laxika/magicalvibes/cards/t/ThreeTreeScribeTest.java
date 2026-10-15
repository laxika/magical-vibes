package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Flicker;
import com.github.laxika.magicalvibes.cards.s.SeasonOfWeaving;
import com.github.laxika.magicalvibes.cards.s.SunshowerDruid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeTreeScribe.class, GrizzlyBears.class, Flicker.class, SeasonOfWeaving.class, SunshowerDruid.class})
class ThreeTreeScribeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a creature you control when another creature leaves without dying")
    void putsCounterWhenAllyLeavesWithoutDying() {
        addCreatureReady(player1, new ThreeTreeScribe());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, leaving.getId());
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
        harness.castAndResolveSorcery(player1, 0, scribe.getId());
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

    @Test
    @DisplayName("Each Scribe triggers for each controlled creature that leaves simultaneously")
    void triggersForEachSimultaneousDepartureIncludingItself() {
        Permanent scribe = addCreatureReady(player1, new ThreeTreeScribe());
        Permanent ally = addCreatureReady(player1, new SunshowerDruid());
        Permanent opponent = addCreatureReady(player2, new SunshowerDruid());
        harness.setHand(player1, java.util.List.of(new SeasonOfWeaving()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 15);
        harness.handlePermanentChosen(player1, scribe.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent tokenScribe = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerHands.get(player1.getId())).contains(scribe.getCard(), ally.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opponent.getCard());

        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, tokenScribe.getId());
        }
        resolveAllTriggers();

        assertThat(tokenScribe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An exiled ally can put a counter on Scribe itself but not an opponent's creature")
    void exileTriggersAndOnlyOffersControlledCreatures() {
        Permanent scribe = addCreatureReady(player1, new ThreeTreeScribe());
        Permanent leaving = addCreatureReady(player1, new SunshowerDruid());
        Permanent opponent = addCreatureReady(player2, new SunshowerDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, leaving));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(scribe.getId())
                .doesNotContain(leaving.getId(), opponent.getId());
        harness.handlePermanentChosen(player1, scribe.getId());
        resolveAllTriggers();

        assertThat(scribe.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Scribe does not trigger for its own death")
    void doesNotTriggerWhenItselfDies() {
        Permanent scribe = addCreatureReady(player1, new ThreeTreeScribe());
        Permanent target = addCreatureReady(player1, new SunshowerDruid());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, scribe));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

}
