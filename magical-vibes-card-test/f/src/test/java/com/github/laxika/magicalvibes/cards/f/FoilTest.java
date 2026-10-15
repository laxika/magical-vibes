package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Foil.class, GrizzlyBears.class, Island.class, Shock.class})
class FoilTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when cast for its mana cost")
    void countersTargetSpellWithNormalCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Foil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Foil");
    }

    @Test
    @DisplayName("Counters the target spell when cast by discarding an Island and another card")
    void countersTargetSpellWithAlternateCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Foil(), new Island(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateDiscards(player2, 0, bears.getId(), 1, List.of(2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Foil");
        harness.assertInGraveyard(player2, "Island");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The alternate cost requires an Island and another distinct card")
    void alternateCostRequiresTwoMatchingCards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Foil(), new Island()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateDiscards(
                player2, 0, bears.getId(), 1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first alternate discard must be an Island card")
    void alternateCostRequiresIsland() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Foil(), new Shock(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateDiscards(
                player2, 0, bears.getId(), 1, List.of(2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Island cards can pay the alternate cost with Foil between them in hand")
    void alternateCostAcceptsTwoIslandsAroundSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        Island firstIsland = new Island();
        Island secondIsland = new Island();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(firstIsland, new Foil(), secondIsland));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateDiscards(player2, 1, bears.getId(), 2, List.of(0));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstIsland, secondIsland);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Foil");
    }

    @Test
    @DisplayName("Foil cannot discard itself to pay its alternate cost")
    void cannotDiscardSpellBeingCast() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Foil(), new Island(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateDiscards(
                player2, 0, bears.getId(), 1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an instant spell without allowing its damage to resolve")
    void countersInstantSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Foil(), new Island(), new Island()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateDiscards(player2, 0, shock.getId(), 1, List.of(2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Foil");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
