package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.z.ZilorthaApexOfIkoria;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CopperHostCrusher.class, DoomedTraveler.class, EliteVanguard.class, GrizzlyBears.class, InvasionOfIkoria.class,
        ShivanDragon.class, ZilorthaApexOfIkoria.class})
class InvasionOfIkoriaTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege searches for a non-Human creature within X from the library")
    void searchesLibraryForEligibleCreature() {
        Card bear = new GrizzlyBears();
        Card human = new DoomedTraveler();
        Card expensive = new ShivanDragon();
        harness.setLibrary(player1, List.of(bear, human, expensive));

        castInvasion(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.pool()).containsExactly(bear);

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).contains(human, expensive);
    }

    @Test
    @DisplayName("The Siege lets its controller choose an eligible creature from the graveyard")
    void searchesGraveyardForEligibleCreature() {
        Card bear = new GrizzlyBears();
        Card human = new DoomedTraveler();
        harness.setGraveyard(player1, List.of(bear, human));
        harness.setLibrary(player1, List.of());

        castInvasion(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.pool()).containsExactly(bear);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Doomed Traveler");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Zilortha transformed")
    void defeatCastsBackFace() {
        harness.setLibrary(player1, List.of());
        castInvasion(0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battle = findPermanent(player1, "Invasion of Ikoria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent zilortha = findPermanent(player1, "Zilortha, Apex of Ikoria");
        assertThat(zilortha.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Zilortha lets a non-Human creature assign combat damage as though unblocked")
    void nonHumanCreatureMayAssignDamageToPlayerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ZilorthaApexOfIkoria());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Zilortha does not grant the assignment choice to Human creatures")
    void humanCreatureCannotAssignDamageToPlayerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new ZilorthaApexOfIkoria());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Trample does not require lethal blocker damage when assigning as though unblocked")
    void tramplerCanAssignAllDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new CopperHostCrusher());
        harness.addToBattlefield(player1, new ZilorthaApexOfIkoria());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ZilorthaApexOfIkoria());
        prepareBlockedCombat(attacker, blocker);

        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 8));

        harness.assertLife(player2, 12);
        harness.assertOnBattlefield(player2, "Zilortha, Apex of Ikoria");
        harness.assertInGraveyard(player1, "Copper Host Crusher");
    }

    @Test
    @DisplayName("Zilortha may apply the assignment choice to itself and still receives blocker damage")
    void zilorthaCanAssignItsOwnDamageAsThoughUnblocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ZilorthaApexOfIkoria());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CopperHostCrusher());
        prepareBlockedCombat(attacker, blocker);

        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 8));

        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player1, "Zilortha, Apex of Ikoria");
        harness.assertOnBattlefield(player2, "Copper Host Crusher");
    }

    @Test
    @DisplayName("A non-Human creature may decline the choice and damage its blocker")
    void canAssignDamageNormallyInstead() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ZilorthaApexOfIkoria());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CopperHostCrusher());
        prepareBlockedCombat(attacker, blocker);

        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 8));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Zilortha, Apex of Ikoria");
        harness.assertInGraveyard(player2, "Copper Host Crusher");
    }

    @Test
    @DisplayName("The defeated Siege's controller may leave it in exile instead of casting it")
    void mayDeclineCastingDefeatedBattle() {
        harness.setLibrary(player1, List.of());
        castInvasion(0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent battle = findPermanent(player1, "Invasion of Ikoria");
        Card card = battle.getCard();
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Zilortha, Apex of Ikoria");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both zones contribute eligible choices, but only one creature may be found")
    void searchesBothZonesAndRejectsMultipleSelections() {
        Card libraryCard = new CopperHostCrusher();
        Card graveyardCard = new CopperHostCrusher();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        castInvasion(8);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.pool()).containsExactlyInAnyOrder(libraryCard, graveyardCard);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(libraryCard.getId(), graveyardCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        harness.assertOnBattlefield(player1, "Copper Host Crusher");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertNotInGraveyard(player1, "Copper Host Crusher");
    }

    @Test
    @DisplayName("An eligible library creature may be left unfound")
    void mayFailToFindInLibrary() {
        Card creature = new CopperHostCrusher();
        harness.setLibrary(player1, List.of(creature));
        harness.setGraveyard(player1, List.of());
        castInvasion(8);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Copper Host Crusher");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X zero cannot find a creature with positive mana value in either zone")
    void zeroXDoesNotFindPositiveManaValueCreature() {
        Card libraryCard = new CopperHostCrusher();
        Card graveyardCard = new CopperHostCrusher();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        castInvasion(0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        harness.assertNotOnBattlefield(player1, "Copper Host Crusher");
    }

    private void prepareBlockedCombat(Permanent attacker, Permanent blocker) {
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private void castInvasion(int xValue) {
        harness.setHand(player1, List.of(new InvasionOfIkoria()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 2);
        gs.playCard(gd, player1, 0, xValue, null, null);
    }
}
