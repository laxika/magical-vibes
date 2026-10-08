package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterkinShaman.class, SuntailHawk.class, FugitiveWizard.class, Levitation.class, Unsummon.class})
class WaterkinShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when a flying creature you control enters")
    void flyingAllyEnteringBoosts() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when a nonflying creature enters")
    void nonFlyingAllyEnteringDoesNotBoost() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's flying creature enters")
    void opponentFlyingCreatureEnteringDoesNotBoost() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new SuntailHawk(), "{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying creature boosts stack and wear off at end of turn")
    void flyingAllyBoostsStackAndWearOff() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());

        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for itself when it enters with flying from Levitation")
    void enteringWithGrantedFlyingBoostsItself() {
        harness.addToBattlefield(player1, new Levitation());

        Permanent shaman = harness.enterBattlefieldAndReturn(player1, new WaterkinShaman());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers for an entering creature that gains flying from Levitation")
    void enteringAllyWithGrantedFlyingBoosts() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());
        harness.addToBattlefield(player1, new Levitation());

        harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost resolves even if the entering flying creature leaves in response")
    void boostResolvesAfterFlyingCreatureLeaves() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new WaterkinShaman());
        Permanent hawk = harness.enterBattlefieldAndReturn(player1, new SuntailHawk());
        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, hawk.getId());
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertInHand(player1, "Suntail Hawk");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(2);
    }
}
