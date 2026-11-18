package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EtherealGuidance.class, DevilthornFox.class})
class EtherealGuidanceTest extends BaseCardTest {

    @Test
    void resolvingBoostsOwnCreaturesButNotOpponents() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(PermanentGrants.powerBoostOf(gd, ownCreature)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ownCreature)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opponentCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentCreature)).isZero();
    }

    @Test
    void boostEndsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
    }

    @Test
    void boostsEveryCreaturePresentAtResolutionButNotLaterArrivals() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        Permanent second = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());
        harness.passBothPriorities();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, later)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, later)).isZero();
    }

    @Test
    void repeatedCastsAccumulateUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new EtherealGuidance(), new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
    }

    @Test
    void resolvesWithNoCreaturesAndDoesNotBoostLaterArrivals() {
        harness.setHand(player1, List.of(new EtherealGuidance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Ethereal Guidance");
        Permanent later = harness.enterBattlefieldAndReturn(player1, new DevilthornFox());
        assertThat(PermanentGrants.powerBoostOf(gd, later)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, later)).isZero();
    }
}
