package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RamblingPossum.class, GrizzlyBears.class})
class RamblingPossumTest extends BaseCardTest {

    @Test
    @DisplayName("A saddled attack boosts Rambling Possum and may return its saddler")
    void saddledAttackBoostsAndReturnsSaddler() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());
        Permanent unrelated = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(possum.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(saddler.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(saddler.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(possum, unrelated);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(saddler.getCard().getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the optional return leaves the saddler on the battlefield")
    void mayDeclineReturn() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());

        saddle(possum);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(possum, saddler);
        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger Rambling Possum")
    void notSaddledDoesNotTrigger() {
        Permanent possum = addCreatureReady(player1, new RamblingPossum());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gqs.getEffectivePower(gd, possum)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, possum)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saddler);
    }

    private void saddle(Permanent possum) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(possum.isSaddled()).isTrue();
    }
}
