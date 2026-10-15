package com.github.laxika.magicalvibes.cards.l;

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

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(armorguard.getPowerModifier()).isEqualTo(1);
        assertThat(armorguard.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost creatures an opponent controls")
    void doesNotBoostOpponents() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());

        castArmorguard();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentBears.getPowerModifier()).isEqualTo(0);
        assertThat(opponentBears.getToughnessModifier()).isEqualTo(0);
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

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive the boost")
    void includesCreaturesEnteringInResponse() {
        castArmorguard();
        harness.passBothPriorities();

        Permanent leotau = harness.enterBattlefieldAndReturn(player1, new GrizzledLeotau());
        harness.passBothPriorities();

        assertThat(leotau.getPowerModifier()).isEqualTo(1);
        assertThat(leotau.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not receive the boost")
    void excludesCreaturesEnteringAfterResolution() {
        castArmorguard();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent leotau = harness.enterBattlefieldAndReturn(player1, new GrizzledLeotau());

        assertThat(leotau.getPowerModifier()).isZero();
        assertThat(leotau.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Entries without casting trigger independently and their boosts add together")
    void repeatedEntriesStackBoosts() {
        Permanent leotau = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new LeoninArmorguard());
        resolveAllTriggers();
        Permanent second = harness.enterBattlefieldAndReturn(player1, new LeoninArmorguard());
        resolveAllTriggers();

        assertThat(leotau.getPowerModifier()).isEqualTo(2);
        assertThat(leotau.getToughnessModifier()).isEqualTo(2);
        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
    }
}
