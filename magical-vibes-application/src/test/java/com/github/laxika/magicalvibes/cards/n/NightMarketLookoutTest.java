package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightMarketLookout.class, WindDrake.class, SkySkiff.class})
class NightMarketLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Night Market Lookout makes each opponent lose 1 life and its controller gain 1 life")
    void tappingLookoutDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new NightMarketLookout());

        tap(lookout);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @CardUsed(WindDrake.class)
    @DisplayName("Tapping another creature does not trigger Night Market Lookout")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new NightMarketLookout());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingTriggersBeforeCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lookout = addCreatureReady(player1, new NightMarketLookout());

        declareAttackers(List.of(0));

        assertThat(lookout.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @CardUsed(SkySkiff.class)
    void summoningSickLookoutTriggersWhenCrewing() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SkySkiff());
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new NightMarketLookout());
        lookout.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(lookout.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @CardUsed(SkySkiff.class)
    void untappingAndCrewingAgainTriggersAgainInSameTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new SkySkiff());
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new NightMarketLookout());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        lookout.untap();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void onlyTappedCopyTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new NightMarketLookout());
        harness.addToBattlefield(player1, new NightMarketLookout());
        harness.addToBattlefield(player2, new NightMarketLookout());

        tap(lookout);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void opponentControlledLookoutGainsLifeForOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lookout = harness.addToBattlefieldAndReturn(player2, new NightMarketLookout());

        tap(lookout);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    @Test
    void triggerResolvesAfterLookoutLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new NightMarketLookout());

        tap(lookout);
        gd.playerBattlefields.get(player1.getId()).remove(lookout);
        gd.playerGraveyards.get(player1.getId()).add(lookout.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
