package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new DominariasJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

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
        harness.setHand(player1, List.of(new DominariasJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

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
        harness.setHand(player1, List.of(new DominariasJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.WHITE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.WHITE)).isFalse();
    }
}
