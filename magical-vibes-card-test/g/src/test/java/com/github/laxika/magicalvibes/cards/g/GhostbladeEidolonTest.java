package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostbladeEidolon.class, GrizzlyBears.class})
class GhostbladeEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Ghostblade Eidolon can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new GhostbladeEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
    }

    @Test
    @DisplayName("Ghostblade Eidolon can be cast for bestow and grants double strike")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostbladeEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent eidolon = findPermanent(player1, "Ghostblade Eidolon");
        assertThat(gqs.isCreature(gd, eidolon)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Ghostblade Eidolon becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostbladeEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent eidolon = findPermanent(player1, "Ghostblade Eidolon");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    void creatureDealsBothCombatDamageSteps() {
        addCreatureReady(player1, new GhostbladeEidolon());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void enchantedCreatureDealsBoostedDamageInBothSteps() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostbladeEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void canEnchantOpponentsCreatureWithoutChangingControllers() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostbladeEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent eidolon = findPermanent(player1, "Ghostblade Eidolon");
        assertThat(eidolon.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
    }

    @Test
    void resolvesAsCreatureWhenBestowTargetLeavesBeforeResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostbladeEidolon()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castWithAlternateCost(player1, 0, bear.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent eidolon = findPermanent(player1, "Ghostblade Eidolon");
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(eidolon.isAttached()).isFalse();
        harness.assertNotInGraveyard(player1, "Ghostblade Eidolon");
    }
}
