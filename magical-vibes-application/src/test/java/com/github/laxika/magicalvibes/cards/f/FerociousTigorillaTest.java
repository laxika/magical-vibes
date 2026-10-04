package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FerociousTigorilla.class)
class FerociousTigorillaTest extends BaseCardTest {

    @Test
    void entersWithTrampleCounterWhenChosen() {
        Permanent tigorilla = castAndChoose("trample");

        assertThat(tigorilla.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(tigorilla.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(tigorilla.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(tigorilla.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    void entersWithMenaceCounterWhenChosen() {
        Permanent tigorilla = castAndChoose("menace");

        assertThat(tigorilla.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(tigorilla.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(tigorilla.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(tigorilla.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void choosesCounterWhenEnteringWithoutBeingCast() {
        Permanent tigorilla = new Permanent(new FerociousTigorilla());
        harness.inMutationScope(() -> {
            harness.getBattlefieldEntryService().putPermanentOntoBattlefield(gd, player2.getId(), tigorilla);
            harness.getBattlefieldEntryService().handleCreatureEnteredBattlefield(
                    gd, player2.getId(), tigorilla.getCard(), null, false);
        });

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("trample", "menace");
        harness.handleListChoice(player2, "menace");

        assertThat(tigorilla.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(tigorilla.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(tigorilla.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(tigorilla.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castAndChoose(String counterType) {
        harness.castFromHand(player1, new FerociousTigorilla(), "{3}{R}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("trample", "menace");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Ferocious Tigorilla");
    }
}
