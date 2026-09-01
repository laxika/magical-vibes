package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.MouseTrapper;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MouseTrapper.class, GiantGrowth.class, GrizzlyBears.class})
class MouseTrapperTest extends BaseCardTest {

    @Test
    void valiantTapsAnOpponentsCreatureWhenTargetedByYourSpell() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, mouseTrapper.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void valiantTriggersOnlyOnceEachTurn() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, mouseTrapper.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstOpponentCreature.getId());
        harness.passBothPriorities();
        assertThat(firstOpponentCreature.isTapped()).isTrue();

        harness.castInstant(player1, 0, mouseTrapper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(secondOpponentCreature.isTapped()).isFalse();
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent mouseTrapper = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, mouseTrapper.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isFalse();
    }
}
