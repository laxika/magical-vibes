package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AccumulateWisdom;
import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GranGran.class, AccumulateWisdom.class, AirbendingLesson.class, Forest.class, GrizzlyBears.class})
class GranGranTest extends BaseCardTest {

    @Test
    @DisplayName("When Gran-Gran becomes tapped, it draws a card and then discards a card")
    void tappingGranGranDrawsAndDiscards() {
        Permanent granGran = harness.addToBattlefieldAndReturn(player1, new GranGran());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        tap(granGran);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Three Lesson cards in the graveyard reduce noncreature spells by {1}")
    void reducesNoncreatureSpellCostWithThreeLessons() {
        harness.addToBattlefield(player1, new GranGran());
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The reduction is inactive with fewer than three Lesson cards")
    void doesNotReduceWithoutThreeLessons() {
        harness.addToBattlefield(player1, new GranGran());
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(
                new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new AccumulateWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to creature spells")
    void doesNotReduceCreatureSpells() {
        harness.addToBattlefield(player1, new GranGran());
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(
                new AirbendingLesson(), new AirbendingLesson(), new AirbendingLesson()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
