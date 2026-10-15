package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuickbeastAmulet.class, GrizzlyBears.class, HillGiant.class, Boomerang.class})
class QuickbeastAmuletTest extends BaseCardTest {

    @Test
    void intensifiesByEnteringCreaturePowerAndBoostsEquippedCreature() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new QuickbeastAmulet());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        amulet.setAttachedTo(bears.getId());

        assertThat(gd.getCardIntensity(amulet.getCard())).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(amulet.getCard())).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    void doesNotIntensifyForAnOpponentCreatureEntering() {
        harness.addToBattlefield(player1, new QuickbeastAmulet());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(findPermanent(player1, "Quickbeast Amulet").getCard())).isZero();
    }

    @Test
    void equipPaysTwoAndGrantsTheAccumulatedBoost() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new QuickbeastAmulet());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(amulet.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void retainsIntensityAfterReturningToHandAndBeingCastAgain() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new QuickbeastAmulet());
        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, amulet.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Quickbeast Amulet");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent returnedAmulet = findPermanent(player1, "Quickbeast Amulet");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(returnedAmulet.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }
}
