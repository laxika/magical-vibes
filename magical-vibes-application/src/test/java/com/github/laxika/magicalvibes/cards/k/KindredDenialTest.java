package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindredDenial.class, Millstone.class, GrizzlyBears.class, GiantGrowth.class, Island.class})
class KindredDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and seeks a card with the same mana value")
    void countersAndSeeksMatchingManaValue() {
        Millstone target = new Millstone();
        GrizzlyBears sought = new GrizzlyBears();
        GiantGrowth differentManaValue = new GiantGrowth();

        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new KindredDenial()));
        harness.setLibrary(player2, List.of(differentManaValue, sought));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Millstone");
        assertThat(gd.playerHands.get(player2.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(differentManaValue);
    }

    @Test
    @DisplayName("Still counters the spell when no matching card can be sought")
    void countersWhenNoMatchingCardExists() {
        Millstone target = new Millstone();
        GiantGrowth differentManaValue = new GiantGrowth();

        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new KindredDenial()));
        harness.setLibrary(player2, List.of(differentManaValue));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Millstone");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(differentManaValue);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player2, List.of(new KindredDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
    }
}
