package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.cards.h.HornetCannon;
import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JinxedRing.class, EnsnaringBridge.class, HornetCannon.class, SkyshroudFalcon.class})
class JinxedRingTest extends BaseCardTest {

    @Test
    @DisplayName("Jinxed Ring deals 1 damage when your nontoken permanent is put into your graveyard")
    void damagesControllerForOwnNontokenPermanent() {
        harness.addToBattlefield(player1, new JinxedRing());
        Permanent bridge = harness.addToBattlefieldAndReturn(player1, new EnsnaringBridge());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bridge));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player1, "Ensnaring Bridge");
    }

    @Test
    @DisplayName("Jinxed Ring ignores nontoken permanents put into an opponent's graveyard")
    void ignoresOpponentGraveyard() {
        harness.addToBattlefield(player1, new JinxedRing());
        Permanent bridge = harness.addToBattlefieldAndReturn(player2, new EnsnaringBridge());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bridge));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player2, "Ensnaring Bridge");
    }

    @Test
    @DisplayName("Jinxed Ring triggers for your permanent put into your graveyard under an opponent's control")
    void damagesForOwnedPermanentControlledByOpponent() {
        harness.addToBattlefield(player1, new JinxedRing());
        EnsnaringBridge bridgeCard = new EnsnaringBridge();
        bridgeCard.setOwnerId(player1.getId());
        Permanent bridge = harness.addToBattlefieldAndReturn(player2, bridgeCard);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bridge));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInGraveyard(player1, "Ensnaring Bridge");
    }

    @Test
    @DisplayName("Sacrifice a creature to give Jinxed Ring to an opponent")
    void sacrificeCreatureGivesRingToOpponent() {
        harness.addToBattlefield(player1, new JinxedRing());
        harness.addToBattlefield(player1, new SkyshroudFalcon());
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Skyshroud Falcon");
        harness.assertOnBattlefield(player2, "Jinxed Ring");
        harness.assertNotOnBattlefield(player1, "Jinxed Ring");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Cannot activate Jinxed Ring without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new JinxedRing());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot give Jinxed Ring to yourself")
    void cannotTargetYourself() {
        harness.addToBattlefield(player1, new JinxedRing());
        harness.addToBattlefield(player1, new SkyshroudFalcon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Skyshroud Falcon");
    }

    @Test
    @DisplayName("Sacrificing a token to Jinxed Ring does not trigger its damage ability")
    void tokenSacrificeDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new JinxedRing());
        harness.addToBattlefield(player1, new HornetCannon());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        Permanent hornet = findPermanent(player1, "Hornet");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(hornet.getId()));
        harness.assertOnBattlefield(player2, "Jinxed Ring");
    }
}
