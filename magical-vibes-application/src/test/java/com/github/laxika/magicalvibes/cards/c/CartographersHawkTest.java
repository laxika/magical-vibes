package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrairieStream;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CartographersHawk.class, Forest.class, Plains.class, PrairieStream.class})
class CartographersHawkTest extends BaseCardTest {

    @Test
    void combatDamageReturnsHawkAndMayPutPlainsOntoBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Cartographer's Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Plains)
                .singleElement()
                .satisfies(land -> assertThat(land.isTapped()).isTrue());
    }

    @Test
    void decliningSearchStillReturnsHawk() {
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Cartographer's Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Plains);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof Plains);
    }

    @Test
    void doesNotTriggerWhenDamagedPlayerDoesNotControlMoreLands() {
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hawk);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(hawk.getCard());
    }

    @Test
    void landCountsAreNotCheckedAgainWhenTriggerResolves() {
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        dealCombatDamage(hawk);

        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cartographer's Hawk");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Plains")).isEqualTo(2);
    }

    @Test
    void gainingLandAdvantageAfterDamageDoesNotCreateATrigger() {
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        dealCombatDamage(hawk);

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hawk);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void cannotSearchIfHawkLeavesBeforeResolution() {
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        dealCombatDamage(hawk);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(hawk);
        gd.playerGraveyards.get(player1.getId()).add(hawk.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cartographer's Hawk");
        harness.assertNotInHand(player1, "Cartographer's Hawk");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    void canFindNonbasicPlainsAndStillPutsItOntoBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new PrairieStream()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        dealCombatDamage(hawk);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Cartographer's Hawk");
        assertThat(findPermanent(player1, "Prairie Stream").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canFailToFindEvenWithPlainsInLibrary() {
        harness.setLibrary(player1, List.of(new Plains()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        dealCombatDamage(hawk);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        harness.assertInHand(player1, "Cartographer's Hawk");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchWithNoMatchingPlainsCompletesWithoutPuttingALandOntoBattlefield() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        dealCombatDamage(hawk);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Cartographer's Hawk");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchWithEmptyLibraryStillReturnsHawkAndCompletes() {
        harness.setLibrary(player1, List.of());
        Permanent hawk = addCreatureReady(player1, new CartographersHawk());
        harness.addToBattlefield(player2, new Forest());
        dealCombatDamage(hawk);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Cartographer's Hawk");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnsToOwnerButControllerSearchesTheirOwnLibrary() {
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLibrary(player2, List.of(new Forest()));
        CartographersHawk card = new CartographersHawk();
        card.setOwnerId(player2.getId());
        Permanent hawk = addCreatureReady(player1, card);
        harness.addToBattlefield(player2, new Forest());
        dealCombatDamage(hawk);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Cartographer's Hawk");
        harness.assertNotInHand(player1, "Cartographer's Hawk");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    private void dealCombatDamage(Permanent hawk) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        hawk.setAttacking(true);
        hawk.setAttackTarget(player2.getId());
        harness.resolveCombatDamage();
    }
}
