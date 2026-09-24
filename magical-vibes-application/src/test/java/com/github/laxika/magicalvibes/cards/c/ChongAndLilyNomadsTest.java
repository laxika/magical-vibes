package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ChongAndLilyNomads.class)
class ChongAndLilyNomadsTest extends BaseCardTest {

    private static final String PUT_LORE_COUNTERS =
            "Put a lore counter on each of any number of target Sagas you control.";
    private static final String BOOST_CREATURES =
            "Creatures you control get +1/+0 until end of turn for each lore counter among Sagas you control.";

    @Test
    void bardAttackCanPutLoreCountersOnAnyNumberOfOwnedSagas() {
        addCreatureReady(player1, new ChongAndLilyNomads());
        addCreatureReady(player1, creature("Bard", CardSubtype.BARD, 2, 2));
        Permanent firstSaga = addSaga(player1, 0);
        Permanent secondSaga = addSaga(player1, 1);
        Permanent opponentSaga = addSaga(player2, 0);

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, PUT_LORE_COUNTERS);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();

        harness.handlePermanentChosen(player1, firstSaga.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(secondSaga.getId())
                .doesNotContain(opponentSaga.getId());
        harness.handlePermanentChosen(player1, secondSaga.getId());
        harness.passBothPriorities();

        assertThat(firstSaga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(secondSaga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(opponentSaga.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    void boostModeUsesLoreCountersOnControlledSagas() {
        Permanent chongAndLily = addCreatureReady(player1, new ChongAndLilyNomads());
        Permanent bard = addCreatureReady(player1, creature("Bard", CardSubtype.BARD, 2, 2));
        addSaga(player1, 2);

        declareAttackers(List.of(1));
        harness.handleListChoice(player1, BOOST_CREATURES);
        harness.passBothPriorities();

        assertThat(chongAndLily.getPowerModifier()).isEqualTo(2);
        assertThat(bard.getPowerModifier()).isEqualTo(2);
        assertThat(chongAndLily.getToughnessModifier()).isZero();
        assertThat(bard.getToughnessModifier()).isZero();
    }

    @Test
    void attackWithoutABardDoesNotTrigger() {
        addCreatureReady(player1, new ChongAndLilyNomads());
        addCreatureReady(player1, creature("Warrior", CardSubtype.WARRIOR, 2, 2));
        addSaga(player1, 0);

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSaga(Player player, int loreCounters) {
        Card saga = new Card();
        saga.setName("Test Saga");
        saga.setType(CardType.ENCHANTMENT);
        saga.setSubtypes(List.of(CardSubtype.SAGA));
        Permanent permanent = harness.addToBattlefieldAndReturn(player, saga);
        permanent.setCounterCount(CounterType.LORE, loreCounters);
        return permanent;
    }

    private Card creature(String name, CardSubtype subtype, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
