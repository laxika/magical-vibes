package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.m.MemoryLapse;
import com.github.laxika.magicalvibes.cards.m.MindbreakTrap;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.n.NomadsEnKor;
import com.github.laxika.magicalvibes.cards.r.Remand;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Boltfire.class, Counterspell.class, MemoryLapse.class, MindbreakTrap.class,
        NarsetParterOfVeils.class, NomadsEnKor.class, Remand.class, SwordsToPlowshares.class})
class BoltfireTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWhenCastFromHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Boltfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Boltfire");
    }

    @Test
    void flashforwardCastsFromExileForItsAlternativeCostAndBottomsTheCard() {
        Boltfire bolt = new Boltfire();
        harness.setLife(player2, 20);
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bolt);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bolt);
        assertThat(gd.findExiledCard(bolt.getId())).isNull();
    }

    @Test
    void flashforwardCardCannotBeCastFromExileUsingItsNormalCost() {
        Boltfire bolt = new Boltfire();
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bolt.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void counteredFlashforwardSpellAlsoGoesToTheBottomOfItsOwnersLibrary() {
        Boltfire bolt = new Boltfire();
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId());

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(bolt);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bolt);
        assertThat(gd.findExiledCard(bolt.getId())).isNull();
    }

    @Test
    void flashforwardPutsTheCardBelowExistingLibraryCards() {
        Boltfire top = new Boltfire();
        Boltfire bottom = new Boltfire();
        Boltfire bolt = new Boltfire();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setExile(player1, List.of(bolt));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromExileWithFlashforward(player1, bolt.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom, bolt);
        assertThat(gd.findExiledCard(bolt.getId())).isNull();
        harness.assertNotInGraveyard(player1, "Boltfire");
    }

    @Test
    void canDealLethalDamageToAnOpposingCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new NomadsEnKor());
        harness.setHand(player1, List.of(new Boltfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Nomads en-Kor");
        harness.assertInGraveyard(player2, "Nomads en-Kor");
        harness.assertInGraveyard(player1, "Boltfire");
    }

    @Test
    void removesTwoLoyaltyFromATargetPlaneswalker() {
        var narset = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Boltfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, narset.getId());

        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Narset, Parter of Veils");
        harness.assertInGraveyard(player1, "Boltfire");
    }

    @Test
    void flashforwardStillBottomsTheCardWhenItsTargetBecomesIllegal() {
        var creature = harness.addToBattlefieldAndReturn(player2, new NomadsEnKor());
        Boltfire bolt = new Boltfire();
        harness.setLibrary(player1, List.of());
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExileWithFlashforward(player1, bolt.getId(), creature.getId());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nomads en-Kor");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bolt);
        harness.assertNotInGraveyard(player1, "Boltfire");
        assertThat(gd.findExiledCard(bolt.getId())).isNull();
    }

    @Test
    void flashforwardCannotBeCastOutsideSorceryTiming() {
        Boltfire bolt = new Boltfire();
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(bolt.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashforwardRequiresTheFullAlternativeCost() {
        Boltfire bolt = new Boltfire();
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(bolt.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remandCannotReturnAFlashforwardSpellToHand() {
        Boltfire bolt = new Boltfire();
        harness.setExile(player1, List.of(bolt));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Counterspell()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId());
        harness.setHand(player2, List.of(new Remand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bolt);
        harness.assertNotInHand(player1, "Boltfire");
        harness.assertNotInGraveyard(player1, "Boltfire");
        harness.assertInHand(player2, "Counterspell");
        harness.assertLife(player2, 20);
    }

    @Test
    void memoryLapseCannotPutAFlashforwardSpellOnTopOfTheLibrary() {
        Boltfire top = new Boltfire();
        Boltfire bolt = new Boltfire();
        harness.setLibrary(player1, List.of(top));
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId());
        harness.setHand(player2, List.of(new MemoryLapse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bolt);
        harness.assertNotInGraveyard(player1, "Boltfire");
    }

    @Test
    void mindbreakTrapCannotExileAFlashforwardSpellAgain() {
        Boltfire bolt = new Boltfire();
        harness.setLibrary(player1, List.of());
        harness.setExile(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExileWithFlashforward(player1, bolt.getId(), player2.getId());
        harness.setHand(player2, List.of(new MindbreakTrap()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, List.of(bolt.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bolt);
        assertThat(gd.findExiledCard(bolt.getId())).isNull();
        harness.assertNotInGraveyard(player1, "Boltfire");
    }
}
