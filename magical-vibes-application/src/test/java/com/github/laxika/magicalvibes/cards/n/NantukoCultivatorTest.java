package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({NantukoCultivator.class, Forest.class, GrizzlyBears.class})
class NantukoCultivatorTest extends BaseCardTest {

    @Test
    void discardingLandCardsPutsCountersOnCultivatorAndDrawsThatManyCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new NantukoCultivator(), new Forest(), new Forest(), new GrizzlyBears()));
        castCultivator();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.XValueChoice countChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(countChoice.maxValue()).isEqualTo(2);
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        Permanent cultivator = findPermanent(player1, "Nantuko Cultivator");
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningTheAbilityDoesNothing() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new NantukoCultivator(), new Forest(), new Forest(), new GrizzlyBears()));
        castCultivator();

        harness.handleMayAbilityChosen(player1, false);

        Permanent cultivator = findPermanent(player1, "Nantuko Cultivator");
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void onlyLandCardsCanBeDiscarded() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new NantukoCultivator(), new Forest(), new GrizzlyBears()));
        castCultivator();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.XValueChoice countChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(countChoice.maxValue()).isEqualTo(1);
        harness.handleXValueChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        Permanent cultivator = findPermanent(player1, "Nantuko Cultivator");
        assertThat(cultivator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void castCultivator() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
