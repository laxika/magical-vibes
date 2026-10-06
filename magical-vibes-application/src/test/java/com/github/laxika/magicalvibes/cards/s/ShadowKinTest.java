package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.h.HordewingSkaab;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowKin.class, CommandTower.class, HordewingSkaab.class, Clone.class})
class ShadowKinTest extends BaseCardTest {

    @Test
    void millsEachPlayerAndCopiesAChosenMilledCreatureWhileRetainingItsAbility() {
        Permanent shadowKin = harness.addToBattlefieldAndReturn(player1, new ShadowKin());
        Card milledCreature = new HordewingSkaab();
        harness.setLibrary(player1, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(milledCreature, new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3).contains(milledCreature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(milledCreature);
        assertThat(shadowKin.getCard().getName()).isEqualTo("Hordewing Skaab");
        assertThat(shadowKin.getCard().getPower()).isEqualTo(3);
        assertThat(shadowKin.getCard().getToughness()).isEqualTo(3);
        harness.setLibrary(player1, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    void decliningCopyLeavesMilledCardsInGraveyards() {
        Permanent shadowKin = harness.addToBattlefieldAndReturn(player1, new ShadowKin());
        Card milledCreature = new HordewingSkaab();
        harness.setLibrary(player1, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(milledCreature, new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(shadowKin.getCard().getName()).isEqualTo("Shadow Kin");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void doesNotOfferCopyChoiceWhenNoCreatureWasMilled() {
        harness.addToBattlefield(player1, new ShadowKin());
        harness.setLibrary(player1, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void exilesAndCopiesDuringTheOriginalResolutionWithoutAnotherStackEntry() {
        Permanent shadowKin = harness.addToBattlefieldAndReturn(player1, new ShadowKin());
        Card creature = new HordewingSkaab();
        harness.setLibrary(player1, List.of(creature, new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(shadowKin.getCard().getName()).isEqualTo("Hordewing Skaab");
    }

    @Test
    void aCloneOfShadowKinRetainsTheUpkeepAbilityAfterBecomingAnotherCreature() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new ShadowKin());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        Permanent clone = findPermanent(player1, "Shadow Kin");
        Card creature = new HordewingSkaab();
        harness.setLibrary(player1, List.of(creature, new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(clone.getCard().getName()).isEqualTo("Hordewing Skaab");

        harness.setLibrary(player1, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        resolveUpkeepTrigger();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    void anOlderCreatureInTheGraveyardCannotBeChosenWhenOnlyLandsAreMilled() {
        harness.addToBattlefield(player1, new ShadowKin());
        Card oldCreature = new HordewingSkaab();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(oldCreature);
    }

    @Test
    void doesNotMillDuringTheOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ShadowKin());
        harness.setLibrary(player1, List.of(new CommandTower(), new CommandTower(), new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower(), new CommandTower()));

        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsAllAvailableCardsFromShortLibraries() {
        harness.addToBattlefield(player1, new ShadowKin());
        harness.setLibrary(player1, List.of(new CommandTower()));
        harness.setLibrary(player2, List.of(new CommandTower(), new CommandTower()));

        resolveUpkeepTrigger();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveUpkeepTrigger() {
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
