package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallOfTheDeathDweller.class, GrizzlyBears.class, LlanowarElves.class, HillGiant.class})
class CallOfTheDeathDwellerTest extends BaseCardTest {

    @Test
    void returnsTwoCreaturesAndLetsYouChooseEachCounterTarget() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.maxTotalManaValue()).isEqualTo(3);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        Permanent returnedElves = findPermanent(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, returnedBears.getId());
        assertThat(returnedBears.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, returnedElves.getId());
        harness.passBothPriorities();

        assertThat(returnedBears.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(returnedElves.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(returnedElves.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    void enforcesTheTargetCountAndCombinedManaValueLimit() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(firstBears, secondBears, hillGiant));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstBears.getId(), secondBears.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");
    }
}
