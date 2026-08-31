package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerryBards.class, GrizzlyBears.class})
class MerryBardsTest extends BaseCardTest {

    @Test
    void payingCreatesYoungHeroRoleOnControlledCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMerryBards();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds())
                .contains(target.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void decliningPaymentDoesNotCreateRole() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMerryBards();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
    }

    @Test
    void youngHeroRolePutsCounterOnAttackingCreatureWithToughnessThreeOrLess() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castMerryBards();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castMerryBards() {
        harness.setHand(player1, List.of(new MerryBards()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
