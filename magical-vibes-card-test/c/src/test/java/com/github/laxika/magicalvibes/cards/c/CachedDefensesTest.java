package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.cards.a.AvenSkirmisher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CachedDefenses.class, AvenSkirmisher.class, FeralKrushok.class})
@DisplayName("Cached Defenses")
class CachedDefensesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts three +1/+1 counters on the creature with the least toughness")
    void bolstersLeastToughCreature() {
        Permanent skirmisher = addCreatureReady(player1, new AvenSkirmisher());
        Permanent krushok = addCreatureReady(player1, new FeralKrushok());

        castCachedDefenses();

        assertThat(skirmisher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(krushok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lets the controller choose among creatures tied for least toughness")
    void choosesAmongLeastToughnessCreatures() {
        Permanent first = addCreatureReady(player1, new AvenSkirmisher());
        Permanent second = addCreatureReady(player1, new AvenSkirmisher());

        castCachedDefenses();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 3, true));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does nothing when the controller has no creatures")
    void doesNothingWithoutCreatures() {
        castCachedDefenses();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Uses toughness including existing counters")
    void usesCurrentToughness() {
        Permanent skirmisher = addCreatureReady(player1, new AvenSkirmisher());
        Permanent krushok = addCreatureReady(player1, new FeralKrushok());
        skirmisher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        castCachedDefenses();

        assertThat(skirmisher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(krushok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignores an opponent's creature with lower toughness")
    void ignoresOpponentsCreatures() {
        Permanent own = addCreatureReady(player1, new FeralKrushok());
        Permanent opposing = addCreatureReady(player2, new AvenSkirmisher());

        castCachedDefenses();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not bolster an opponent's creature when you control none")
    void doesNothingWithOnlyOpposingCreatures() {
        Permanent opposing = addCreatureReady(player2, new AvenSkirmisher());

        castCachedDefenses();

        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Marked damage does not reduce toughness for bolster")
    void ignoresMarkedDamage() {
        Permanent skirmisher = addCreatureReady(player1, new AvenSkirmisher());
        Permanent krushok = addCreatureReady(player1, new FeralKrushok());
        skirmisher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        krushok.setMarkedDamage(3);

        castCachedDefenses();

        assertThat(skirmisher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(krushok.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCachedDefenses() {
        harness.setHand(player1, List.of(new CachedDefenses()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
