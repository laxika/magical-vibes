package com.github.laxika.magicalvibes.cards.i;

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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomedTraveler.class, EliteVanguard.class, GrizzlyBears.class, InvasionOfIkoria.class,
        ShivanDragon.class, ZilorthaApexOfIkoria.class})
class InvasionOfIkoriaTest extends BaseCardTest {

    @Test
    @DisplayName("The Siege searches for a non-Human creature within X from the library")
    void searchesLibraryForEligibleCreature() {
        Card bear = new GrizzlyBears();
        Card human = new DoomedTraveler();
        Card expensive = new ShivanDragon();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bear, human, expensive));

        castInvasion(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.pool()).containsExactly(bear);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardsChosen(List.of(bear.getId())));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).contains(human, expensive);
    }

    @Test
    @DisplayName("The Siege lets its controller choose an eligible creature from the graveyard")
    void searchesGraveyardForEligibleCreature() {
        Card bear = new GrizzlyBears();
        Card human = new DoomedTraveler();
        harness.setGraveyard(player1, List.of(bear, human));
        gd.playerDecks.get(player1.getId()).clear();

        castInvasion(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice search =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.pool()).containsExactly(bear);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardsChosen(List.of(bear.getId())));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Doomed Traveler");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Defeating the Siege exiles it and casts Zilortha transformed")
    void defeatCastsBackFace() {
        gd.playerDecks.get(player1.getId()).clear();
        castInvasion(0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battle = findPermanent(player1, "Invasion of Ikoria");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zilortha = findPermanent(player1, "Zilortha, Apex of Ikoria");
        assertThat(zilortha.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Zilortha lets a non-Human creature assign combat damage as though unblocked")
    void nonHumanCreatureMayAssignDamageToPlayerWhenBlocked() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ZilorthaApexOfIkoria());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
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
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new ZilorthaApexOfIkoria());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
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

    private void castInvasion(int xValue) {
        harness.setHand(player1, List.of(new InvasionOfIkoria()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 2);
        gs.playCard(gd, player1, 0, xValue, null, null);
    }
}
