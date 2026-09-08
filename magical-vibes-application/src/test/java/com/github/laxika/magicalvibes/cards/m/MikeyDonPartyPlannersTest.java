package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DonatelloTurtleTechie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MikeyDonPartyPlanners.class, DonatelloTurtleTechie.class, Forest.class, GrizzlyBears.class})
class MikeyDonPartyPlannersTest extends BaseCardTest {

    @Test
    @DisplayName("Plays a land from the top of the library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("Casts a Mutant, Ninja, or Turtle creature from the top with an extra counter")
    void castsPartyCreatureWithAdditionalCounter() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        Card donatello = new DonatelloTurtleTechie();
        harness.setLibrary(player1, List.of(donatello));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveFromLibraryTop(player1);

        Permanent permanent = findPermanent(player1, "Donatello, Turtle Techie");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast a creature without a Mutant, Ninja, or Turtle subtype from the top")
    void cannotCastNonPartyCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Does not add a counter when the party creature is cast from hand")
    void doesNotAddCounterToHandCast() {
        harness.addToBattlefield(player1, new MikeyDonPartyPlanners());
        harness.setHand(player1, List.of(new DonatelloTurtleTechie()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Donatello, Turtle Techie");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
