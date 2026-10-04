package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NorthPolePatrol;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatalFissure.class, Forest.class, GrizzlyBears.class, NorthPolePatrol.class})
class FatalFissureTest extends BaseCardTest {

    @Test
    @DisplayName("Earthbends a land controlled by Fatal Fissure's caster when the target dies")
    void earthbendsCasterLandWhenTargetDies() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent casterLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FatalFissure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, victim.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, victim));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .contains(casterLand.getId())
                .doesNotContain(opponentLand.getId());

        harness.handlePermanentChosen(player1, casterLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, casterLand)).isTrue();
        assertThat(gqs.isCreature(gd, casterLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, casterLand)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, casterLand)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, casterLand, Keyword.HASTE)).isTrue();
        assertThat(casterLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void doesNotFollowCreatureThatLeavesAndReturns() {
        NorthPolePatrol creature = new NorthPolePatrol();
        Permanent victim = harness.addToBattlefieldAndReturn(player1, creature);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FatalFissure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, victim));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent returned = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "North Pole Patrol"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void earthbendedLandReturnsTappedWithoutAnimationOrCounters(boolean exile) {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new NorthPolePatrol());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FatalFissure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, victim));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, land)).isTrue();

        harness.inMutationScope(() -> {
            if (exile) {
                harness.getPermanentRemovalService().removePermanentToExile(gd, land);
            } else {
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land);
            }
        });
        harness.passBothPriorities();

        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest"));
        assertThat(returned).isNotNull();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotEarthbendWhenTargetDiesBeforeSpellResolves() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new NorthPolePatrol());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FatalFissure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, victim));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void delayedDeathTriggerExpiresAtEndOfTurn() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new NorthPolePatrol());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new FatalFissure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, victim));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void exilingTargetDoesNotTriggerEarthbend() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new NorthPolePatrol());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FatalFissure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, victim));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }
}
