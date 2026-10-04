package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GleamingBarrier;
import com.github.laxika.magicalvibes.cards.n.NezahalPrimalTide;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hornswoggle.class, GrizzlyBears.class, Millstone.class, GleamingBarrier.class, NezahalPrimalTide.class})
class HornswoggleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and creates one Treasure token")
    void countersCreatureSpellAndCreatesOneTreasure() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no Treasure if the target spell leaves the stack")
    void createsNoTreasureIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player2, "Hornswoggle");
    }

    @Test
    @DisplayName("Treasure token is an artifact with Treasure subtype")
    void treasureTokenHasTreasureType() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        Permanent treasure = findPermanent(player2, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }
    @Test
    @DisplayName("Creates Treasure even when the creature spell cannot be countered")
    void createsTreasureForUncounterableCreature() {
        NezahalPrimalTide nezahal = new NezahalPrimalTide();
        harness.setHand(player1, List.of(nezahal));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, nezahal.getId());

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        harness.assertNotInGraveyard(player1, "Nezahal, Primal Tide");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nezahal, Primal Tide");
    }

    @Test
    @DisplayName("Counters an artifact creature without triggering its dies ability")
    void countersArtifactCreature() {
        GleamingBarrier barrier = new GleamingBarrier();
        harness.setHand(player1, List.of(barrier));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, barrier.getId());

        harness.assertInGraveyard(player1, "Gleaming Barrier");
        harness.assertNotOnBattlefield(player1, "Gleaming Barrier");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Treasure can immediately be tapped and sacrificed for colored mana")
    void treasureCanImmediatelyProduceMana() {
        GleamingBarrier barrier = new GleamingBarrier();
        harness.setHand(player1, List.of(barrier));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, barrier.getId());

        assertThat(findPermanent(player2, "Treasure").isTapped()).isFalse();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "RED");

        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
