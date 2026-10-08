package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SongbirdSonicScreamer.class})
class SongbirdSonicScreamerTest extends BaseCardTest {

    @Test
    void discardingACardGrantsFlyingUntilEndOfTurn() {
        Permanent songbird = harness.addToBattlefieldAndReturn(player1, new SongbirdSonicScreamer());
        harness.setHand(player1, List.of(new SongbirdSonicScreamer()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, songbird, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Songbird, Sonic Screamer");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, songbird, Keyword.FLYING)).isFalse();
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new SongbirdSonicScreamer());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    void tappedSummoningSickSongbirdPaysDiscardBeforeFlyingResolves() {
        Permanent songbird = harness.addToBattlefieldAndReturn(player1, new SongbirdSonicScreamer());
        songbird.setSummoningSick(true);
        songbird.setTapped(true);
        harness.setHand(player1, List.of(new SongbirdSonicScreamer()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Songbird, Sonic Screamer");
        harness.assertNotInHand(player1, "Songbird, Sonic Screamer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, songbird, Keyword.FLYING)).isFalse();
        assertThat(songbird.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, songbird, Keyword.FLYING)).isTrue();
        assertThat(songbird.isTapped()).isTrue();
    }

    @Test
    void unblockedCombatDamageGainsLife() {
        addCreatureReady(player1, new SongbirdSonicScreamer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }
}
