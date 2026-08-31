package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StolenGoodies;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PicnicRuiner.class, StolenGoodies.class, GrizzlyBears.class, CrawWurm.class})
class PicnicRuinerTest extends BaseCardTest {

    @Test
    void adventureDistributesThreeCountersAmongControlledCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        PicnicRuiner card = new PicnicRuiner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, 0, Map.of(first.getId(), 1, second.getId(), 2));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCanBeCastWithNoTargets() {
        PicnicRuiner card = new PicnicRuiner();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PicnicRuiner()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, 0, Map.of(opponentCreature.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingWithAControlledCreatureOfPowerFourGivesDoubleStrike() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void attackDoesNotGiveDoubleStrikeWithoutAControlledCreatureOfPowerFour() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent ruiner = addCreatureReady(player1, new PicnicRuiner());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, ruiner, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
