package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravityNegator.class, GrizzlyBears.class})
class GravityNegatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets another creature, not Gravity Negator")
    void attackTargetsAnotherCreature() {
        Permanent source = addCreatureReady(player1, new GravityNegator());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(ownBears.getId(), opponentBears.getId())
                .doesNotContain(source.getId());
    }

    @Test
    @DisplayName("Paying {C} gives the target creature flying until end of turn")
    void payingColorlessManaGrantsFlying() {
        addCreatureReady(player1, new GravityNegator());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining the payment does not give the target creature flying")
    void decliningPaymentDoesNotGrantFlying() {
        addCreatureReady(player1, new GravityNegator());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }
}
