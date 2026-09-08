package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AirliftChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and offers a milled Plains for the hand")
    void acceptsMilledPlains() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new Shock());

        Permanent chaplain = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB offers a creature with mana value three or less")
    void acceptsLowManaValueCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new SerraAngel(), new Forest());

        Permanent chaplain = castAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Serra Angel", "Forest");
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the only eligible card puts a +1/+1 counter on Airlift Chaplain")
    void declinesEligibleCardAndGetsCounter() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new Shock());

        Permanent chaplain = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter is only put on the creature after all eligible offers are declined")
    void declinesAllEligibleCardsBeforeCounter() {
        setLibrary(new Plains(), new GrizzlyBears(), new Forest());

        Permanent chaplain = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No eligible milled card automatically puts a +1/+1 counter on Airlift Chaplain")
    void noEligibleCardGetsCounter() {
        setLibrary(new Forest(), new Shock(), new SerraAngel());

        Permanent chaplain = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(chaplain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private Permanent castAndResolveEtb() {
        harness.setHand(player1, List.of(new AirliftChaplain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Airlift Chaplain");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
