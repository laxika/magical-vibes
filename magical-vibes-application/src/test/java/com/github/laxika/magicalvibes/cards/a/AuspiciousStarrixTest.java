package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuspiciousStarrix.class, Forest.class, GrizzlyBears.class, Shock.class})
class AuspiciousStarrixTest extends BaseCardTest {

    @Test
    @DisplayName("A mutation exiles until one permanent is found and puts it onto the battlefield")
    void mutationFindsOnePermanent() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card shock = new Shock();
        Card forest = new Forest();
        Card remainingShock = new Shock();
        harness.setLibrary(player1, List.of(shock, forest, remainingShock));

        triggerMutation(starrix);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(forest.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shock.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(remainingShock);
    }

    @Test
    @DisplayName("The number of permanents found scales with the mutation count")
    void mutationCountControlsPermanentCount() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card shockOne = new Shock();
        Card forestOne = new Forest();
        Card shockTwo = new Shock();
        Card bears = new GrizzlyBears();
        Card forestTwo = new Forest();
        harness.setLibrary(player1, List.of(shockOne, forestOne, shockTwo, bears, forestTwo));

        triggerMutationWithoutResolving(starrix);
        triggerMutationWithoutResolving(starrix);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(starrix.getCard().getId(), forestOne.getId(), bears.getId(), forestTwo.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shockOne.getId(), shockTwo.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void triggerMutation(Permanent starrix) {
        triggerMutationWithoutResolving(starrix);
        resolveAllTriggers();
    }

    private void triggerMutationWithoutResolving(Permanent starrix) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, starrix, List.of(starrix.getCard()), player1.getId()));
    }
}
