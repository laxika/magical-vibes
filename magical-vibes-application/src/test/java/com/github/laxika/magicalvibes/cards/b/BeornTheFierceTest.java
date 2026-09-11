package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeornTheFierce.class, GrizzlyBears.class, HillGiant.class})
class BeornTheFierceTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other Bears you control")
    void boostsOtherBearsYouControl() {
        Permanent beorn = addCreatureReady(player1, new BeornTheFierce());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beorn)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beorn)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds a trample counter and Bear subtype before checking the draw threshold")
    void addsCounterAndSubtypeThenDrawsTwoCards() {
        addCreatureReady(player1, new BeornTheFierce());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.BEAR);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Choosing no target skips the target effects and does not draw below three Bears")
    void choosingNoTargetSkipsTargetEffectsAndDraw() {
        addCreatureReady(player1, new BeornTheFierce());
        Permanent existingBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(existingBear.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(existingBear.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(existingBear.getGrantedSubtypes()).doesNotContain(CardSubtype.BEAR);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
