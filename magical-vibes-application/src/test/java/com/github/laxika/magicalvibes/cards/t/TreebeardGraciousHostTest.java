package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvenOfEnduringHope;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreebeardGraciousHost.class, AvenOfEnduringHope.class, RedwoodTreefolk.class, GrizzlyBears.class})
class TreebeardGraciousHostTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Food tokens")
    void entersWithTwoFoodTokens() {
        harness.setHand(player1, List.of(new TreebeardGraciousHost()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(2);
        assertThat(findPermanents(player1, "Food")).allSatisfy(food -> {
            assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        });
    }

    @Test
    @DisplayName("Life gain puts that many counters on a target Halfling or Treefolk")
    void lifeGainPutsCountersEqualToLifeGained() {
        harness.addToBattlefield(player1, new TreebeardGraciousHost());
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new RedwoodTreefolk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvenOfEnduringHope()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(treefolk.getId());
        assertThat(choice.validIds()).doesNotContain(
                harness.getPermanentId(player1, "Grizzly Bears"));

        harness.handlePermanentChosen(player1, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
