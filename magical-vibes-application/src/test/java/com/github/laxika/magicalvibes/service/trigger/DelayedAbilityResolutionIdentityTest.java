package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A delayed triggered ability is created by the resolution of a spell or ability (CR 603.7a), so the delayed actions
 * one resolution of a source schedules are one stack object ("exile them at the beginning of the next end step") and
 * those another resolution of the same source schedules are another. {@code GameData.queueDelayedAction} stamps
 * each action with the resolution in progress ({@code GameData.resolutionOrdinal}) and the queues group by it.
 */
@CardUsed({GrizzlyBears.class})
class DelayedAbilityResolutionIdentityTest extends BaseCardTest {

    private static Card source() {
        Card card = new Card();
        card.setName("Twice Resolved");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}");
        return card;
    }

    private List<StackEntry> delayedAbilitiesOf(Card source) {
        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard() == source)
                .toList();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = DelayedPermanentActionKind.class, names = {"SACRIFICE_AT_NEXT_UPKEEP",
            "EXILE_TOKEN_AT_END_STEP", "SACRIFICE_AT_END_STEP", "EXILE_TOKEN_AT_END_OF_COMBAT",
            "DESTROY_AT_END_OF_COMBAT", "RETURN_TO_HAND_AT_END_STEP"})
    @DisplayName("Two resolutions of one source are two delayed abilities; one resolution's permanents are one")
    void delayedPermanentActionsAreGroupedByTheResolutionThatScheduledThem(DelayedPermanentActionKind kind) {
        Card source = source();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        gd.resolutionOrdinal = 11;
        gd.queueDelayedAction(new DelayedPermanentAction(first.getId(), kind, false, null, null, null, false, source));
        gd.queueDelayedAction(new DelayedPermanentAction(second.getId(), kind, false, null, null, null, false, source));
        gd.resolutionOrdinal = 12;
        gd.queueDelayedAction(new DelayedPermanentAction(third.getId(), kind, false, null, null, null, false, source));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().processDelayedPermanentActions(gd, kind));

        assertThat(delayedAbilitiesOf(source)).hasSize(2);
        assertThat(delayedAbilitiesOf(source)).extracting(entry -> entry.getEffectsToResolve().size())
                .containsExactly(2, 1);
    }

    @Test
    @DisplayName("\"Exile it at the beginning of your next upkeep\" of two resolutions of one source is two abilities")
    void upkeepExilesAreGroupedByTheResolutionThatScheduledThem() {
        Card source = source();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());

        gd.resolutionOrdinal = 11;
        gd.queueDelayedAction(new ExilePermanentAtNextUpkeep(player1.getId(), first.getId(), source));
        gd.queueDelayedAction(new ExilePermanentAtNextUpkeep(player1.getId(), second.getId(), source));
        gd.resolutionOrdinal = 12;
        gd.queueDelayedAction(new ExilePermanentAtNextUpkeep(player1.getId(), third.getId(), source));

        advanceToUpkeep(player1);

        assertThat(delayedAbilitiesOf(source)).hasSize(2);
        assertThat(delayedAbilitiesOf(source)).extracting(entry -> entry.getTargetIds().size())
                .containsExactlyInAnyOrder(2, 1);
    }
}
