package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.OreskosSunGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlitterstepEidolon.class, OreskosSunGuide.class})
class FlitterstepEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Flitterstep Eidolon can be cast normally and can't be blocked")
    void castsNormallyAsUnblockableCreature() {
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, eidolon)).isTrue();
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and makes it unblockable")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, bear)).isTrue();
    }

    @Test
    void canBestowOnOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(eidolon.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.isCreature(gd, eidolon)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasCantBeBlocked(gd, bear)).isTrue();
    }

    @Test
    void becomesUnblockableCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear).findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
        assertThat(eidolon.isAttached()).isFalse();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, eidolon)).isTrue();
        harness.assertNotInGraveyard(player1, "Flitterstep Eidolon");
    }

    @Test
    void resolvesAsUnblockableCreatureWhenBestowTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castWithAlternateCost(player1, 0, bear.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(eidolon.isAttached()).isFalse();
        assertThat(gqs.isCreature(gd, eidolon)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, eidolon)).isTrue();
        harness.assertNotInGraveyard(player1, "Flitterstep Eidolon");
    }

    @Test
    void hostLosesBoostAndUnblockabilityWhenEidolonLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        harness.setHand(player1, List.of(new FlitterstepEidolon()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent eidolon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != bear).findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, eidolon));
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, bear)).isFalse();
    }
}
