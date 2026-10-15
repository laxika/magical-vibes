package com.github.laxika.magicalvibes.cards.m;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.w.WallOfRazors;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Mortuary.class, MoxDiamond.class, WallOfRazors.class})
class MortuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a creature that dies on top of its owner's library")
    void putsDyingCreatureOnTopOfLibrary() {
        Card libraryCard = new MoxDiamond();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addToBattlefield(player1, new Mortuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WallOfRazors());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature.getCard(), libraryCard);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent is put into the graveyard")
    void doesNotTriggerForNoncreaturePermanent() {
        Card libraryCard = new MoxDiamond();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addToBattlefield(player1, new Mortuary());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MoxDiamond());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does nothing if the dying creature leaves the graveyard before resolution")
    void doesNothingIfDyingCreatureLeavesGraveyard() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new Mortuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WallOfRazors());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        Card deadCard = creature.getCard();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardByIdForExile(gd, deadCard.getId()));
        gd.addToExile(player1.getId(), deadCard);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(deadCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Binds each dying creature to its own trigger")
    void bindsEachDyingCreatureToItsOwnTrigger() {
        Card libraryCard = new MoxDiamond();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addToBattlefield(player1, new Mortuary());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new WallOfRazors());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new WallOfRazors());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstCreature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondCreature);
        });
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstCreature.getCard(), secondCreature.getCard(), libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers for a creature you own that dies under an opponent's control")
    void triggersForOwnedCreatureControlledByOpponent() {
        Card libraryCard = new MoxDiamond();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addToBattlefield(player1, new Mortuary());
        Card creatureCard = new WallOfRazors();
        creatureCard.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature.getCard(), libraryCard);
    }

    @Test
    @DisplayName("Does not trigger for an opponent-owned creature that dies under your control")
    void doesNotTriggerForCreatureYouDoNotOwn() {
        harness.addToBattlefield(player1, new Mortuary());
        Card creatureCard = new WallOfRazors();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, creatureCard);
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creatureCard);
    }

    @Test
    @DisplayName("A pending trigger still resolves after Mortuary leaves the battlefield")
    void resolvesAfterMortuaryLeavesBattlefield() {
        Card libraryCard = new MoxDiamond();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent mortuary = harness.addToBattlefieldAndReturn(player1, new Mortuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WallOfRazors());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mortuary);
        });
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature.getCard(), libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(mortuary.getCard());
    }

    @Test
    @DisplayName("Does not trigger for a creature card put into the graveyard without dying")
    void doesNotTriggerForCreatureCardFromAnotherZone() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new Mortuary());
        Card creature = new WallOfRazors();

        harness.setGraveyard(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Cannot find a creature that leaves the graveyard and dies again before resolution")
    void doesNotMoveNewGraveyardIncarnation() {
        Card libraryCard = new MoxDiamond();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent mortuary = harness.addToBattlefieldAndReturn(player1, new Mortuary());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WallOfRazors());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mortuary);
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, creature.getCard().getId());
        });
        Permanent returnedCreature = harness.addToBattlefieldAndReturn(player1, creature.getCard());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returnedCreature));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(mortuary.getCard(), creature.getCard());
    }
}
