package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EverflameEidolon.class, GrizzlyBears.class})
class EverflameEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Everflame Eidolon can be cast normally and activated for +1/+0")
    void castsNormallyAndBoostsItself() {
        harness.setHand(player1, List.of(new EverflameEidolon()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and its ability boosts the host")
    void castsForBestowAndBoostsEnchantedCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EverflameEidolon()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("A bestowed Everflame Eidolon becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EverflameEidolon()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear)
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    void repeatedActivationsExpireAtEndOfTurn() {
        Permanent eidolon = harness.addToBattlefieldAndReturn(player1, new EverflameEidolon());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(1);
    }

    @Test
    void bestowedPumpExpiresButStaticBonusRemainsOnOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new EverflameEidolon());
        harness.setHand(player1, List.of(new EverflameEidolon()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    void pendingPumpBoostsOpponentsHostAfterAuraLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new EverflameEidolon());
        harness.setHand(player1, List.of(new EverflameEidolon()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Everflame Eidolon");
    }

    @Test
    void pendingPumpBoostsEidolonAfterHostLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new EverflameEidolon());
        harness.setHand(player1, List.of(new EverflameEidolon()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(1);
    }

    @Test
    void bestowResolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new EverflameEidolon());
        harness.setHand(player1, List.of(new EverflameEidolon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(1);
    }
}
