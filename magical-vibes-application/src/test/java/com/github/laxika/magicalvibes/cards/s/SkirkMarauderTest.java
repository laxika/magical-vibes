package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BranchsnapLorian;
import com.github.laxika.magicalvibes.cards.d.DelugeOfTheDead;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.w.WrennAndRealmbreaker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BranchsnapLorian.class, DelugeOfTheDead.class, InvasionOfInnistrad.class,
        SkirkMarauder.class, WrennAndRealmbreaker.class})
class SkirkMarauderTest extends BaseCardTest {

    @Test
    void turningFaceUpDealsTwoDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BranchsnapLorian());
        Permanent marauder = castFaceDown();

        turnFaceUp(marauder);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void turningFaceUpDealsTwoDamageToTargetPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WrennAndRealmbreaker());
        target.setCounterCount(CounterType.LOYALTY, 4);
        Permanent marauder = castFaceDown();

        turnFaceUp(marauder);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void turningFaceUpDealsTwoDamageToTargetBattle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        target.setCounterCount(CounterType.DEFENSE, 5);
        Permanent marauder = castFaceDown();

        turnFaceUp(marauder);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    void turningFaceUpDealsTwoDamageToTargetPlayer() {
        Permanent marauder = castFaceDown();

        turnFaceUp(marauder);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new SkirkMarauder()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Skirk Marauder");
    }

    private void turnFaceUp(Permanent marauder) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(marauder));
    }
}
