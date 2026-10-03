package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurrowguardMentor.class, GrizzlyBears.class, Forest.class, BarkformHarvester.class})
class BurrowguardMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Burrowguard Mentor is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent mentor = addMentorReady(player1);

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only creatures controlled by its controller")
    void countsOnlyControllersCreatures() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(1);
    }

    @Test
    void doesNotCountNoncreaturePermanents() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new BarkformHarvester());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(2);
    }

    @Test
    void countersAreAddedAfterCreatureCountAndRemainWhenCountChanges() {
        Permanent mentor = addMentorReady(player1);
        mentor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);

        harness.addToBattlefield(player1, new BurrowguardMentor());

        assertThat(gqs.getEffectivePower(gd, mentor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(4);
    }

    @Test
    void characteristicAbilityWorksInHandAndGraveyardWithoutCountingItself() {
        BurrowguardMentor inHand = new BurrowguardMentor();
        BurrowguardMentor inGraveyard = new BurrowguardMentor();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new BurrowguardMentor());
        harness.addToBattlefield(player2, new BurrowguardMentor());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(1);
    }

    @Test
    void tramplesOverBlockerUsingCurrentCreatureCount() {
        Permanent mentor = addMentorReady(player1);
        harness.addToBattlefield(player1, new BurrowguardMentor());
        harness.addToBattlefield(player1, new BurrowguardMentor());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BurrowguardMentor());
        mentor.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Burrowguard Mentor");
        assertThat(gqs.getEffectiveToughness(gd, mentor)).isEqualTo(3);
    }

    private Permanent addMentorReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BurrowguardMentor());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
