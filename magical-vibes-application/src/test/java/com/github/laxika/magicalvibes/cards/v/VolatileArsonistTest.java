package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DireStrainAnarchist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolatileArsonist.class, DireStrainAnarchist.class, GrizzlyBears.class, JaceBeleren.class})
class VolatileArsonistTest extends BaseCardTest {

    @Test
    void frontFaceDealsOneDamageToEachChosenTargetType() {
        Permanent arsonist = addCreatureReady(player1, new VolatileArsonist());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(arsonist)));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void backFaceDealsTwoDamageToEachChosenTargetType() {
        Permanent arsonist = addTransformedArsonist();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(arsonist)));

        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void dayNightTransformsBothFaces() {
        gd.dayNight = DayNight.DAY;
        Permanent arsonist = addCreatureReady(player1, new VolatileArsonist());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(arsonist.isTransformed()).isTrue();
        assertThat(arsonist.getCard()).isInstanceOf(DireStrainAnarchist.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(arsonist.isTransformed()).isFalse();
        assertThat(arsonist.getCard()).isInstanceOf(VolatileArsonist.class);
    }

    private Permanent addTransformedArsonist() {
        VolatileArsonist card = new VolatileArsonist();
        Permanent arsonist = addCreatureReady(player1, card);
        arsonist.setCard(card.getBackFaceCard());
        arsonist.setTransformed(true);
        return arsonist;
    }
}
