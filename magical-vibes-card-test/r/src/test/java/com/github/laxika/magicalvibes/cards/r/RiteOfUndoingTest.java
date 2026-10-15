package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiteOfUndoing.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class RiteOfUndoingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent controlled by each player to its owner's hand")
    void bouncesOnePermanentControlledByEachPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(ownPermanentId, opposingPermanentId));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Rejects land and same-controller targets")
    void rejectsIllegalTargets() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forestId, opposingPermanentId)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownPermanentId, ownPermanentId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Delve pays all four generic mana while blue mana is paid normally")
    void delvePaysGenericMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        List<Card> graveyard = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(ownId, opposingId), List.of(),
                false, null, null, null, null, List.of(0, 1, 2, 3));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Rite of Undoing");
    }

    @Test
    @DisplayName("The opposing target still returns when the caster loses control of the first target")
    void firstTargetBecomesIllegalAfterControlChange() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(ownId, opposingId));

        var moved = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerBattlefields.get(player2.getId()).add(moved);
        gd.stolenCreatures.put(ownId, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Rite of Undoing");
    }

    @Test
    @DisplayName("The first target still returns when the caster gains control of the second target")
    void secondTargetBecomesIllegalAfterControlChange() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(ownId, opposingId));

        var moved = gd.playerBattlefields.get(player2.getId()).removeFirst();
        gd.playerBattlefields.get(player1.getId()).add(moved);
        gd.stolenCreatures.put(opposingId, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Rite of Undoing");
    }

    @Test
    @DisplayName("A borrowed permanent returns to its owner rather than its controller")
    void returnsBorrowedPermanentToOwner() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID ownId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingId = harness.getPermanentId(player2, "Llanowar Elves");
        gd.stolenCreatures.put(ownId, player2.getId());
        harness.setHand(player1, List.of(new RiteOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, List.of(ownId, opposingId));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertInHand(player2, "Llanowar Elves");
    }
}
