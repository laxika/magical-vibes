package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowsbreathOgre.class})
class BellowsbreathOgreTest extends BaseCardTest {

    @Test
    void startsAtIntensityOneDealsThatMuchThenIntensifies() {
        Permanent target = addCreatureReady(player2, new BellowsbreathOgre());
        Permanent ogre = harness.enterBattlefieldAndReturn(player1, new BellowsbreathOgre());
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(ogre.getCard())).isEqualTo(1);
        ogre.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getCardIntensity(ogre.getCard())).isEqualTo(2);
    }

    @Test
    void attackDamageUsesCurrentIntensity() {
        Permanent ogre = harness.enterBattlefieldAndReturn(player1, new BellowsbreathOgre());
        harness.passBothPriorities();
        ogre.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new BellowsbreathOgre());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            gd.intensifyCard(ogre.getCard(), 1);
            harness.passBothPriorities();
        });

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getCardIntensity(ogre.getCard())).isEqualTo(3);
    }

    @Test
    void intensityPersistsAfterDyingAndReturningToBattlefield() {
        Permanent target = addCreatureReady(player2, new BellowsbreathOgre());
        Permanent ogre = harness.enterBattlefieldAndReturn(player1, new BellowsbreathOgre());
        harness.passBothPriorities();
        ogre.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        });

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ogre));
        gd.playerGraveyards.get(player1.getId()).remove(ogre.getCard());
        Permanent returned = harness.enterBattlefieldAndReturn(player1, ogre.getCard());
        harness.passBothPriorities();
        returned.setSummoningSick(false);
        Permanent freshTarget = addCreatureReady(player2, new BellowsbreathOgre());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, freshTarget.getId());
            harness.passBothPriorities();
        });

        assertThat(freshTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getCardIntensity(returned.getCard())).isEqualTo(3);
    }

    @Test
    void attackTriggerStillDealsDamageAndIntensifiesAfterSourceDies() {
        Permanent target = addCreatureReady(player2, new BellowsbreathOgre());
        Permanent ogre = harness.enterBattlefieldAndReturn(player1, new BellowsbreathOgre());
        harness.passBothPriorities();
        ogre.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.inMutationScope(() -> harness.getPermanentRemovalService()
                    .removePermanentToGraveyard(gd, ogre));
            harness.passBothPriorities();
        });

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getCardIntensity(ogre.getCard())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Bellowsbreath Ogre");
    }
}
