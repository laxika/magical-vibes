package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiresideInfiltrator.class, DukharaPeafowl.class})
class SpiresideInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped deals 1 damage to each opponent")
    void becomingTappedDealsDamageToEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SpiresideInfiltrator());

        tap(infiltrator);

        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Spireside Infiltrator")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpiresideInfiltrator());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingTriggersDamageBeforeCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SpiresideInfiltrator());
        infiltrator.setSummoningSick(false);
        harness.addToBattlefield(player2, new DukharaPeafowl());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(infiltrator.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void anotherCopyDoesNotTriggerWhenThisCopyBecomesTapped() {
        harness.setLife(player2, 20);
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SpiresideInfiltrator());
        harness.addToBattlefield(player1, new SpiresideInfiltrator());

        tap(infiltrator);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    void triggersAgainAfterUntappingAndBecomingTappedAgain() {
        harness.setLife(player2, 20);
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new SpiresideInfiltrator());

        tap(infiltrator);
        resolveAllTriggers();
        infiltrator.untap();
        tap(infiltrator);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void opponentControlledInfiltratorDamagesItsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player2, new SpiresideInfiltrator());

        tap(infiltrator);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
