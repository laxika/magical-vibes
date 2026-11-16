package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.github.laxika.magicalvibes.testutil.DelayedPermanentMoves;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A delayed triggered ability is created by the resolution of a spell or ability (CR 603.7a), so the permanent moves
 * one resolution of a source schedules are one delayed ability ("exile them at the beginning of the next end step")
 * and those another resolution of the same source schedules are another. {@code DelayedPermanentActions} groups them
 * by the resolution in progress ({@code GameData.resolutionOrdinal}).
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
        DelayedPermanentActions.schedule(gd, player1.getId(), source, null,
                new DelayedPermanentAction(first.getId(), kind), ActivePlayer.ANY);
        DelayedPermanentActions.schedule(gd, player1.getId(), source, null,
                new DelayedPermanentAction(second.getId(), kind), ActivePlayer.ANY);
        gd.resolutionOrdinal = 12;
        DelayedPermanentActions.schedule(gd, player1.getId(), source, null,
                new DelayedPermanentAction(third.getId(), kind), ActivePlayer.ANY);

        assertThat(DelayedPermanentMoves.abilities(gd)).hasSize(2);
        assertThat(DelayedPermanentMoves.abilities(gd)).extracting(delayed -> delayed.ability().effects().size())
                .containsExactly(2, 1);
    }
}
