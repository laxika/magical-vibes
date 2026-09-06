package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenDragonslayer.class, HillGiant.class, GrizzlyBears.class})
class HiddenDragonslayerTest extends BaseCardTest {

    @Test
    void megamorphPutsCounterOnItAndDestroysTargetOpponentCreatureWithPowerAtLeastFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent dragonslayer = castFaceDown();

        turnFaceUp(dragonslayer);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(dragonslayer.isFaceDown()).isFalse();
        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void megamorphHasNoTargetWhenOnlyOwnOrLowPowerCreaturesExist() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        ownCreature.setPowerModifier(1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent dragonslayer = castFaceDown();

        turnFaceUp(dragonslayer);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(dragonslayer.isFaceDown()).isFalse();
        assertThat(dragonslayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new HiddenDragonslayer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Hidden Dragonslayer");
    }

    private void turnFaceUp(Permanent dragonslayer) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragonslayer));
    }
}
