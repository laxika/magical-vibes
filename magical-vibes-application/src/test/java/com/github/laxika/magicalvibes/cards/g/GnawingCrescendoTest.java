package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RatOut;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnawingCrescendo.class, RatOut.class, GrizzlyBears.class})
class GnawingCrescendoTest extends BaseCardTest {

    @Test
    void boostsYourCreaturesAndCreatesNonblockingRatsForYourNontokenDeaths() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGnawingCrescendo();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);

        opponentCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(findPermanents(player1, "Rat")).isEmpty();

        ownCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(bls.canBlock(gd, rats.getFirst())).isFalse();
    }

    @Test
    void doesNotTriggerWhenATokenCreatureDies() {
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent rat = findPermanents(player1, "Rat").getFirst();
        harness.setHand(player1, List.of(new GnawingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        rat.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void pumpExpiresAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGnawingCrescendo();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    private void castGnawingCrescendo() {
        harness.setHand(player1, List.of(new GnawingCrescendo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
