package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OstrichHorse.class, Forest.class, GrizzlyBears.class, Shock.class})
class OstrichHorseTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards and lets you put a milled land into your hand")
    void returnsMilledLandToHand() {
        Forest forest = new Forest();
        setLibrary(forest, new GrizzlyBears(), new Shock());

        Permanent ostrichHorse = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(ostrichHorse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the milled land puts a +1/+1 counter on Ostrich-Horse")
    void declinesMilledLandAndGetsCounter() {
        Forest forest = new Forest();
        setLibrary(forest, new GrizzlyBears(), new Shock());

        Permanent ostrichHorse = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(ostrichHorse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No milled land automatically puts a +1/+1 counter on Ostrich-Horse")
    void noMilledLandGetsCounter() {
        setLibrary(new GrizzlyBears(), new Shock(), new GrizzlyBears());

        Permanent ostrichHorse = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ostrichHorse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private Permanent castAndResolveEtb() {
        harness.castFromHand(player1, new OstrichHorse(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ostrich-Horse");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    @Test
    void mayChooseSecondMilledLandWithoutGettingCounter() {
        Forest first = new Forest();
        Forest second = new Forest();
        setLibrary(first, second, new OstrichHorse());

        Permanent source = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second).hasSize(2);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingFirstLandEndsOffersAndReturnsOnlyOneLand() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        setLibrary(first, second, third);

        Permanent source = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second, third);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningEveryLandAddsExactlyOneCounter() {
        setLibrary(new Forest(), new Forest(), new Forest());

        Permanent source = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryStillAddsCounter() {
        setLibrary();

        Permanent source = castAndResolveEtb();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void shortLibraryCanStillReturnMilledLand() {
        Forest forest = new Forest();
        setLibrary(forest);

        Permanent source = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
