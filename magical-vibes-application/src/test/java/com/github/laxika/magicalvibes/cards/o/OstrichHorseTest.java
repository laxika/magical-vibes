package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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
        harness.setHand(player1, List.of(new OstrichHorse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ostrich-Horse");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
