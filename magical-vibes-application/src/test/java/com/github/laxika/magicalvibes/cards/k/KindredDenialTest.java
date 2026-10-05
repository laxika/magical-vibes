package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.v.VexyrIchTekiksHeir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindredDenial.class, Millstone.class, GrizzlyBears.class, GiantGrowth.class, Island.class,
        DarksteelColossus.class, Banefire.class, VexyrIchTekiksHeir.class})
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
        harness.castAndResolveInstant(player2, 0, target.getId());

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
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player1, "Millstone");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(differentManaValue);
    }

    @Test
    @DisplayName("Countering happens before seeking, including library replacement effects")
    void seeksOwnCounteredSpellAfterItIsShuffledIntoLibrary() {
        DarksteelColossus target = new DarksteelColossus();
        harness.setHand(player1, List.of(target, new KindredDenial()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 13);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Darksteel Colossus");
        harness.assertNotOnBattlefield(player1, "Darksteel Colossus");
        harness.assertInGraveyard(player1, "Kindred Denial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An uncounterable spell still allows seeking, with X included in its mana value")
    void seeksMatchingManaValueOfUncounterableXSpell() {
        Banefire target = new Banefire();
        DarksteelColossus sought = new DarksteelColossus();
        GiantGrowth differentManaValue = new GiantGrowth();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.setHand(player2, List.of(new KindredDenial()));
        harness.setLibrary(player2, List.of(differentManaValue, sought));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 10, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(differentManaValue);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Banefire");

        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player1, "Banefire");
    }

    @Test
    @DisplayName("Seeking a matching card triggers abilities that care about seeking")
    void seekingTriggersVexyr() {
        Millstone target = new Millstone();
        GrizzlyBears sought = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player2, new VexyrIchTekiksHeir());
        harness.setHand(player2, List.of(new KindredDenial()));
        harness.setLibrary(player2, List.of(sought));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Millstone");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(sought);
        harness.assertOnBattlefield(player2, "Phyrexian Golem");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Phyrexian Golem"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Does not seek if the target spell has already left the stack")
    void doesNotSeekWhenTargetBecomesIllegal() {
        Millstone target = new Millstone();
        GrizzlyBears matchingCard = new GrizzlyBears();
        Island zeroManaValueCard = new Island();
        harness.setHand(player1, List.of(target, new KindredDenial()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new KindredDenial()));
        harness.setLibrary(player2, List.of(matchingCard, zeroManaValueCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player2, "Kindred Denial");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(matchingCard, zeroManaValueCard);
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
