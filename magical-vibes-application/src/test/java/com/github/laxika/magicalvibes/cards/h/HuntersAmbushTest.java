package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntersAmbush.class, HillGiant.class, GrizzlyBears.class, Ornithopter.class})
class HuntersAmbushTest extends BaseCardTest {

    private void castAmbush() {
        harness.setHand(player1, List.of(new HuntersAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("Nongreen creatures are prevented from dealing combat damage")
    void nongreenCreaturesPrevented() {
        harness.addToBattlefield(player2, new HillGiant());
        Permanent giant = findPermanent(player2, "Hill Giant");

        castAmbush();

        assertThat(gqs.isPreventedFromDealingDamage(gd, giant, true)).isTrue();
    }

    @Test
    @DisplayName("Colorless creatures are nongreen and are also prevented")
    void colorlessCreaturesPrevented() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent thopter = findPermanent(player2, "Ornithopter");

        castAmbush();

        assertThat(gqs.isPreventedFromDealingDamage(gd, thopter, true)).isTrue();
    }

    @Test
    @DisplayName("Green creatures still deal combat damage")
    void greenCreaturesUnaffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        castAmbush();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    @Test
    @DisplayName("Nongreen creatures can still deal noncombat damage")
    void noncombatDamageUnaffected() {
        harness.addToBattlefield(player2, new HillGiant());
        Permanent giant = findPermanent(player2, "Hill Giant");

        castAmbush();

        assertThat(gqs.isPreventedFromDealingDamage(gd, giant, false)).isFalse();
    }

    @Test
    @DisplayName("Hunter's Ambush goes to the graveyard after resolving")
    void goesToGraveyard() {
        castAmbush();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hunter's Ambush");
    }

    @Test
    @DisplayName("Only green unblocked attackers deal combat damage")
    void onlyGreenAttackersDealDamage() {
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        castAmbush();

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevention applies to nongreen creatures entering after resolution on either side")
    void laterCreaturesAreAlsoAffected() {
        castAmbush();
        Permanent ownGiant = harness.enterBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGiant = harness.enterBattlefieldAndReturn(player2, new HillGiant());
        Permanent opposingBears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.isPreventedFromDealingDamage(gd, ownGiant, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, opposingGiant, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, opposingBears, true)).isFalse();
    }

    @Test
    @DisplayName("Nongreen creatures can deal combat damage again after turn cleanup")
    void preventionExpiresAtEndOfTurn() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castAmbush();
        assertThat(gqs.isPreventedFromDealingDamage(gd, giant, true)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, giant, true)).isFalse();
    }
}
