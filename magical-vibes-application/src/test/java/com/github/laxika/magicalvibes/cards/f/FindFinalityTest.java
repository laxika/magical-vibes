package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FindFinalityTest extends BaseCardTest {

    private static final int FIND = 0;
    private static final int FINALITY = 1;

    @Test
    @DisplayName("Find returns up to two target creature cards from the graveyard")
    void findReturnsTwoCreatureCards() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(first, second, third, land));
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, FIND, List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second).doesNotContain(third, land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third, land);
    }

    @Test
    @DisplayName("Finality optionally adds counters before shrinking every creature")
    void finalityAddsCountersAndShrinksAllCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castFinality();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Finality still shrinks creatures when its counter choice is declined")
    void finalityMayBeDeclined() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        Permanent opposingCreature = addCreatureReady(player2, new AvatarOfMight());
        castFinality();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Finality's creature shrink wears off at end of turn")
    void finalityShrinkWearsOff() {
        Permanent ownCreature = addCreatureReady(player1, new AvatarOfMight());
        castFinality();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(10);
    }

    private void castFinality() {
        harness.setHand(player1, List.of(new FindFinality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, FINALITY, List.of());
    }
}
