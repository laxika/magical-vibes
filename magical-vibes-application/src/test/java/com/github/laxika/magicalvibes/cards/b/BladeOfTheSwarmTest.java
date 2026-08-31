package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeOfTheSwarm.class})
class BladeOfTheSwarmTest extends BaseCardTest {

    @Test
    void putsTwoPlusOneCountersOnItself() {
        castBladeOfTheSwarm();

        harness.handleListChoice(player1, "Put two +1/+1 counters on this creature.");
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Blade of the Swarm");
        assertThat(blade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void putsTargetWarpCardOnBottomOfItsOwnersLibrary() {
        Card warpCard = cardWithKeyword("Warp card", Keyword.WARP);
        Card nonWarpCard = cardWithKeyword("Non-warp card");
        Card existingLibraryCard = cardWithKeyword("Existing library card");
        harness.setExile(player2, List.of(nonWarpCard, warpCard));
        harness.setLibrary(player2, List.of(existingLibraryCard));

        castBladeOfTheSwarm();

        harness.handleListChoice(player1,
                "Put target exiled card with warp on the bottom of its owner's library.");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonWarpCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, warpCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonWarpCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingLibraryCard, warpCard);
    }

    private void castBladeOfTheSwarm() {
        harness.setHand(player1, List.of(new BladeOfTheSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Card cardWithKeyword(String name, Keyword... keywords) {
        Card card = new Card();
        card.setName(name);
        card.setKeywords(Set.of(keywords));
        return card;
    }
}
