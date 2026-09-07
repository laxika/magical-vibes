package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, GrizzlyBears.class, Island.class, RevelRuiner.class})
class RevelRuinerTest extends BaseCardTest {

    @Test
    void entersAndConnivesPuttingCounterOnItAfterNonlandDiscard() {
        Card drawn = new Island();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new RevelRuiner(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent revelRuiner = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId)).contains(drawn.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(discarded.getId());
        assertThat(revelRuiner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersAndConnivesWithoutCounterAfterLandDiscard() {
        Card drawn = new Island();
        Card discarded = new Forest();
        harness.setHand(player1, List.of(new RevelRuiner(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent revelRuiner = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getId))
                .contains(discarded.getId());
        assertThat(revelRuiner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
