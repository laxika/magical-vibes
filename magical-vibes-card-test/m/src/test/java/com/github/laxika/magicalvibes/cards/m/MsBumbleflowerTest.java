package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MsBumbleflower.class, GrizzlyBears.class})
class MsBumbleflowerTest extends BaseCardTest {

    @Test
    void spellCastTriggerTargetsAnOpponentAndCreature() {
        Permanent msBumbleflower = addCreatureReady(player1, new MsBumbleflower());
        Permanent targetCreature = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears opponentDraw = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(targetCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(msBumbleflower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void secondResolutionDrawsTwoCards() {
        addCreatureReady(player1, new MsBumbleflower());
        Permanent targetCreature = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        castAndResolveTrigger(targetCreature);
        castAndResolveTrigger(targetCreature);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(targetCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(targetCreature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    private void castAndResolveTrigger(Permanent targetCreature) {
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, targetCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
