package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LochLarent.class, Forest.class, GrizzlyBears.class})
class LochLarentTest extends BaseCardTest {

    @Test
    @DisplayName("Loch Larent enters tapped and taps for blue mana")
    void entersTappedAndTapsForBlueMana() {
        harness.setHand(player1, List.of(new LochLarent()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Loch Larent");
        assertThat(land.isTapped()).isTrue();

        land.enterUntapped();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loch Larent's one-time boon makes the next opponent creature enter tapped with a stun counter")
    void oneTimeBoonModifiesNextOpponentCreatureSpell() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new LochLarent());
        land.enterUntapped();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent firstBear = findPermanents(player2, "Grizzly Bears").getFirst();
        assertThat(firstBear.isTapped()).isTrue();
        assertThat(firstBear.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent secondBear = findPermanents(player2, "Grizzly Bears").get(1);
        assertThat(secondBear.isTapped()).isFalse();
        assertThat(secondBear.getCounterCount(CounterType.STUN)).isZero();
    }
}
