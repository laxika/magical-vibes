package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrumgullyTheGenerous.class, GrizzlyBears.class, FugitiveWizard.class})
class GrumgullyTheGenerousTest extends BaseCardTest {

    @Test
    void otherNonHumanCreatureYouControlEntersWithCounter() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void humanCreatureYouControlDoesNotGetCounter() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsNonHumanCreatureDoesNotGetCounter() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void grumgullyDoesNotGiveItselfCounter() {
        Permanent grumgully = harness.enterBattlefieldAndReturn(player1, new GrumgullyTheGenerous());

        assertThat(grumgully.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void creatureEnteringWithoutBeingCastAlreadyHasCounterBeforePriority() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void existingCreaturesDoNotGetCountersWhenGrumgullyEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrumgullyTheGenerous());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void anotherGrumgullyEntersWithCounterFromTheExistingOne() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrumgullyTheGenerous());

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureDoesNotGetCounterWhenGrumgullyLeavesBeforeItResolves() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
