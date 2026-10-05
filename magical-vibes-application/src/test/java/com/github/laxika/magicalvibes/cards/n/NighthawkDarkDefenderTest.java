package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BuckyBarnesEagerAlly;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NighthawkDarkDefender.class, BuckyBarnesEagerAlly.class, GrizzlyBears.class, Conspiracy.class})
class NighthawkDarkDefenderTest extends BaseCardTest {

    @Test
    void ownEntryBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new NighthawkDarkDefender(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void anotherHeroEntryBoostsTargetCreature() {
        harness.addToBattlefield(player1, new NighthawkDarkDefender());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BuckyBarnesEagerAlly(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void nonHeroEntryDoesNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NighthawkDarkDefender());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentHeroEntryDoesNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NighthawkDarkDefender());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new BuckyBarnesEagerAlly(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownEntryCanTargetItself() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new NighthawkDarkDefender());
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(2);
    }

    @Test
    void canBoostOpponentsCreatureAndBoostExpiresAfterTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BuckyBarnesEagerAlly());
        harness.castFromHand(player1, new NighthawkDarkDefender(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void ownEntryTriggersEvenWhenConspiracyReplacesHeroType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BuckyBarnesEagerAlly());

        harness.castFromHand(player1, new NighthawkDarkDefender(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
}
