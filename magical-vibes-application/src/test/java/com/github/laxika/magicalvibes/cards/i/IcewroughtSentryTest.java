package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IcewroughtSentry.class, GrizzlyBears.class})
class IcewroughtSentryTest extends BaseCardTest {

    @Test
    void paidAttackTriggerTapsAnOpponentsCreatureAndBoostsSentry() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(opponentCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(4);
    }

    @Test
    void decliningAttackPaymentDoesNotTapOrBoost() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }

    @Test
    void tappingOpponentsCreatureByOpponentsEffectDoesNotBoostSentry() {
        Permanent sentry = addCreatureReady(player1, new IcewroughtSentry());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        opponentCreature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, opponentCreature, player2.getId()));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(3);
    }
}
