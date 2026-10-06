package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WhispererOfTheWilds;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandsteppeMastodon.class, WhispererOfTheWilds.class})
class SandsteppeMastodonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and bolsters the creature with the least toughness")
    void entersAndBolstersLeastToughnessCreature() {
        Permanent whisperer = addCreatureReady(player1, new WhispererOfTheWilds());

        castMastodon();
        resolveAllTriggers();

        assertThat(whisperer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bolster can put its counters on the entering Mastodon itself")
    void bolstersItselfWhenAlone() {
        castMastodon();
        resolveAllTriggers();

        Permanent mastodon = findPermanent(player1, "Sandsteppe Mastodon");
        assertThat(mastodon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bolster ignores an opponent's creature with lower toughness")
    void ignoresOpposingCreature() {
        Permanent opponent = addCreatureReady(player2, new WhispererOfTheWilds());

        castMastodon();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Sandsteppe Mastodon")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A toughness tie requires choosing exactly one creature, including the entering Mastodon")
    void choosesExactlyOneAmongTiedCreatures() {
        Permanent first = addCreatureReady(player1, new SandsteppeMastodon());

        castMastodon();
        resolveAllTriggers();

        Permanent entering = findPermanents(player1, "Sandsteppe Mastodon").get(1);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), entering.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(first.getId(), entering.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(entering.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Bolster compares current toughness when the enter trigger resolves")
    void evaluatesCurrentToughnessAtResolution() {
        Permanent whisperer = addCreatureReady(player1, new WhispererOfTheWilds());

        castMastodon();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(whisperer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        whisperer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        resolveAllTriggers();

        assertThat(whisperer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanent(player1, "Sandsteppe Mastodon")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private void castMastodon() {
        harness.setHand(player1, List.of(new SandsteppeMastodon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
