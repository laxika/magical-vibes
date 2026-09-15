package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DominariasJudgment.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class,
        Plains.class, Swamp.class})
class DominariasJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Grants each applicable protection to creatures you control")
    void grantsProtectionForControlledBasicLandTypes() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new DominariasJudgment(), "{2}{W}");
        harness.passBothPriorities();

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, ownCreature, color)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, otherOwnCreature, color)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, opponentCreature, color)).isFalse();
        }
    }

    @Test
    @DisplayName("Only controlled basic land types enable their protection")
    void doesNotGrantProtectionWithoutMatchingBasicLand() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.castFromHand(player1, new DominariasJudgment(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Protection expires at end of turn")
    void protectionExpiresAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Plains());
        harness.castFromHand(player1, new DominariasJudgment(), "{2}{W}");
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.WHITE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Uses only lands controlled by the spell's controller")
    void ignoresMatchingLandsControlledByOpponent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new DominariasJudgment(), "{2}{W}");
        harness.passBothPriorities();

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, ownCreature, color)).isFalse();
        }
    }

    @Test
    @DisplayName("Checks land control on resolution and grants only to creatures present then")
    void checksLandControlOnResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new DominariasJudgment(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, existingCreature, CardColor.WHITE)).isFalse();

        harness.addToBattlefield(player1, new Plains());
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasProtectionFrom(gd, existingCreature, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, laterCreature, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Protection remains after the matching land leaves the battlefield")
    void protectionRemainsAfterMatchingLandLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.castFromHand(player1, new DominariasJudgment(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.WHITE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(plains);

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.WHITE)).isTrue();
    }
}
