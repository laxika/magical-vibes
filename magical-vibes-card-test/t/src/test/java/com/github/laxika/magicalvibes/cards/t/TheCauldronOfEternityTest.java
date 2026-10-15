package com.github.laxika.magicalvibes.cards.t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TheCauldronOfEternity.class, Gingerbrute.class, Forest.class})
class TheCauldronOfEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Costs two less for each creature card in the controller's graveyard")
    void costReductionCountsCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Gingerbrute(), new Gingerbrute()));
        harness.setHand(player1, List.of(new TheCauldronOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Puts a controlled creature that dies on the bottom of its owner's library")
    void putsControlledCreatureOnOwnersLibraryBottom() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, creature.getCard());
    }

    @Test
    @DisplayName("Puts a controlled creature on the opponent owner's library bottom")
    void putsStolenCreatureOnItsOwnersLibraryBottom() {
        Card forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());
        gd.playerBattlefields.get(player2.getId()).remove(stolenCreature);
        gd.playerBattlefields.get(player1.getId()).add(stolenCreature);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, stolenCreature));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(stolenCreature.getCard().getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest, stolenCreature.getCard());
    }

    @Test
    @DisplayName("Reanimates a target creature card and pays the activation costs")
    void reanimatesTargetCreature() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new TheCauldronOfEternity());
        Card creature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cauldron), 0,
                null, creature.getId(), Zone.GRAVEYARD);
        assertThat(cauldron.isTapped()).isTrue();
        harness.assertLife(player1, 8);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(8);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new TheCauldronOfEternity());
        Card noncreature = new TheCauldronOfEternity();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(cauldron), 0,
                null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    void fiveCreaturesReduceCostToTwoBlackMana() {
        harness.setGraveyard(player1, List.of(new Gingerbrute(), new Gingerbrute(),
                new Gingerbrute(), new Gingerbrute(), new Gingerbrute(), new Forest()));
        harness.setHand(player1, List.of(new TheCauldronOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsCreaturesAndOwnNoncreaturesDoNotReduceCost() {
        harness.setGraveyard(player1, List.of(new Forest(), new TheCauldronOfEternity()));
        harness.setGraveyard(player2, List.of(new Gingerbrute(), new Gingerbrute()));
        harness.setHand(player1, List.of(new TheCauldronOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castArtifact(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void deathTriggerDoesNothingIfCreatureLeavesGraveyard() {
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        assertThat(gd.stack).hasSize(1);
        gd.playerGraveyards.get(player1.getId()).remove(creature.getCard());
        harness.setExile(player1, List.of(creature.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(creature.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
    }

    @Test
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Card creature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotPayTwoLifeWhenOnlyOneLifeRemains() {
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Card creature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReanimateOpponentsCreatureCard() {
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Card creature = new Gingerbrute();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Card creature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        harness.addToBattlefield(player1, new TheCauldronOfEternity());
        Card creature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new TheCauldronOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void removedTargetIsNotReanimatedAndCostsRemainPaid() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new TheCauldronOfEternity());
        Card creature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player1.getId()).remove(creature);
        harness.setExile(player1, List.of(creature));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gingerbrute");
        harness.assertLife(player1, 18);
        assertThat(cauldron.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void deathTriggerStillResolvesAfterCauldronLeavesBattlefield() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new TheCauldronOfEternity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, cauldron));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).last().isSameAs(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cauldron.getCard());
    }

    @Test
    void deathTriggerDoesNotMoveCreatureThatLeftAndReenteredGraveyard() {
        Permanent cauldron = harness.addToBattlefieldAndReturn(player1, new TheCauldronOfEternity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardById(gd, creature.getCard().getId()));
        Permanent returnedCreature = harness.enterBattlefieldAndReturn(player1, creature.getCard());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, cauldron));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returnedCreature));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(creature.getCard());
    }

    @Test
    void moreThanFiveCreaturesStillRequireBothBlackMana() {
        harness.setGraveyard(player1, List.of(new Gingerbrute(), new Gingerbrute(),
                new Gingerbrute(), new Gingerbrute(), new Gingerbrute(), new Gingerbrute()));
        harness.setHand(player1, List.of(new TheCauldronOfEternity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
