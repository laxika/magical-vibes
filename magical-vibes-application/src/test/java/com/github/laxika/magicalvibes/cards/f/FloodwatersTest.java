package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Floodwaters.class, DuneBeetle.class, Island.class})
class FloodwatersTest extends BaseCardTest {

    @Test
    @DisplayName("Can resolve without choosing any targets")
    void resolvesWithZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());

        harness.castFromHand(player1, new Floodwaters(), "{4}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Floodwaters");
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void rejectsThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void rejectsDuplicateTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns creatures controlled by either player to their owners")
    void returnsCreaturesToOwnersRatherThanControllers() {
        DuneBeetle stolenCard = new DuneBeetle();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, stolenCard);
        DuneBeetle ownCard = new DuneBeetle();
        ownCard.setOwnerId(player1.getId());
        Permanent own = harness.addToBattlefieldAndReturn(player2, ownCard);
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(stolen.getId(), own.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolenCard);
    }

    @Test
    @DisplayName("Still returns the remaining legal target after another leaves the battlefield")
    void returnsRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.setGraveyard(player2, List.of(first.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first.getCard());
    }

    @Test
    @DisplayName("Does not return a target that has left the battlefield")
    void doesNotReturnAnIllegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, List.of(creature.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature.getCard());
        harness.assertInGraveyard(player1, "Floodwaters");
    }

    @Test
    @DisplayName("Cycling pays the discard before the draw resolves")
    void cyclingDiscardsAsCost() {
        Floodwaters card = new Floodwaters();
        DuneBeetle draw = new DuneBeetle();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        Floodwaters card = new Floodwaters();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Floodwaters");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoCreatures() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(bear1.getId(), bear2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Dune Beetle"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Returns a single target creature to its owner's hand (up to two)")
    void returnsOneCreature() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(bear1.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bear2);
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Dune Beetle"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Floodwaters()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Floodwaters");
        harness.assertInHand(player1, "Dune Beetle");
    }
}
