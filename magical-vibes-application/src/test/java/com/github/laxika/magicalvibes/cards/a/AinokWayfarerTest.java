package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({AinokWayfarer.class, Forest.class, Plains.class, Shock.class})
class AinokWayfarerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and offers a milled land for the hand")
    void acceptsMilledLand() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new Shock());

        Permanent wayfarer = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining a milled land puts a +1/+1 counter on Ainok Wayfarer")
    void declinesMilledLandAndGetsCounter() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new Shock());

        Permanent wayfarer = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No milled land automatically puts a +1/+1 counter on Ainok Wayfarer")
    void noMilledLandGetsCounter() {
        setLibrary(new Shock(), new AinokWayfarer(), new Shock());

        Permanent wayfarer = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private Permanent castAndResolveEtb() {
        harness.setHand(player1, List.of(new AinokWayfarer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ainok Wayfarer");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
