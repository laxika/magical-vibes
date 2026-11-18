package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Leonin Armorguard")
@CardUsed({LeoninArmorguard.class, GrizzledLeotau.class})
class LeoninArmorguardTest extends BaseCardTest {

    private void castArmorguard() {
        harness.castFromHand(player1, new LeoninArmorguard(), "{2}{G}{W}");
    }

    @Test
    @DisplayName("ETB gives creatures you control +1/+1 until end of turn, including itself")
    void etbBoostsOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());

        castArmorguard();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB boost

        Permanent armorguard = findPermanent(player1, "Leonin Armorguard");

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, armorguard)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, armorguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());

        castArmorguard();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, opponentBears)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentBears)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());

        castArmorguard();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive the boost")
    void includesCreaturesEnteringInResponse() {
        castArmorguard();
        harness.passBothPriorities();

        Permanent leotau = harness.enterBattlefieldAndReturn(player1, new GrizzledLeotau());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, leotau)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, leotau)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not receive the boost")
    void excludesCreaturesEnteringAfterResolution() {
        castArmorguard();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent leotau = harness.enterBattlefieldAndReturn(player1, new GrizzledLeotau());

        assertThat(PermanentGrants.powerBoostOf(gd, leotau)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, leotau)).isZero();
    }

    @Test
    @DisplayName("Entries without casting trigger independently and their boosts add together")
    void repeatedEntriesStackBoosts() {
        Permanent leotau = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new LeoninArmorguard());
        resolveAllTriggers();
        Permanent second = harness.enterBattlefieldAndReturn(player1, new LeoninArmorguard());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, leotau)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, leotau)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
    }
}
