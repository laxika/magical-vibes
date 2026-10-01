package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArtilleryEnthusiast.class, GrizzlyBears.class, HillGiant.class})
class ArtilleryEnthusiastTest extends BaseCardTest {

    @Test
    void modifiedCreaturesYouControlHaveMenace() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unmodified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentModified = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentModified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modified, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodified, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentModified, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isFalse();
    }

    @Test
    void mayDiscardToSeekCardWithTheDiscardedCardsManaValue() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears matching = new GrizzlyBears();
        HillGiant nonmatching = new HillGiant();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(nonmatching, matching));

        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    void decliningDiscardLeavesHandAndLibraryUnchanged() {
        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        GrizzlyBears card = new GrizzlyBears();
        HillGiant libraryCard = new HillGiant();
        harness.setHand(player1, new ArrayList<>(List.of(card)));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
