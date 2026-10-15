package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DanceWithDevils;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurzothChaosRider.class, DanceWithDevils.class, GrizzlyBears.class})
class ZurzothChaosRiderTest extends BaseCardTest {

    @Test
    void createsADevilWhenAnOpponentDrawsTheirFirstCardOutsideTheirTurn() {
        harness.addToBattlefield(player1, new ZurzothChaosRider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        draw(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Devil")).hasSize(1);
    }

    @Test
    void doesNotCreateADevilForTheOpponentsFirstDrawOnTheirTurn() {
        harness.addToBattlefield(player1, new ZurzothChaosRider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        draw(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    void triggersOnlyForTheFirstOpponentDrawEachTurn() {
        harness.addToBattlefield(player1, new ZurzothChaosRider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        resolveAllTriggers();
        draw(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Devil")).hasSize(1);
    }

    @Test
    void DevilsAttackingMakeBothPlayersDrawAndRandomlyDiscard() {
        harness.addToBattlefield(player1, new ZurzothChaosRider());
        createDevilToken();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent devil = findPermanent(player1, "Devil");
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(devil)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void nonDevilAttacksDoNotTriggerTheDrawAndDiscardAbility() {
        harness.addToBattlefield(player1, new ZurzothChaosRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void controllerDrawingDoesNotCreateADevil() {
        harness.addToBattlefield(player1, new ZurzothChaosRider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        draw(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    void firstDrawBeforeZurzothEntersStillCountsForTheTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player2);
        harness.addToBattlefield(player1, new ZurzothChaosRider());

        draw(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Devil")).isEmpty();
    }

    @Test
    void bothPlayersDrawBeforeEitherPlayerDiscards() {
        Permanent zurzoth = addCreatureReady(player1, new ZurzothChaosRider());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int logStart = gd.gameLog.size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zurzoth)));
        resolveAllTriggers();

        List<String> drawAndDiscardEvents = gd.gameLog.stream().skip(logStart)
                .map(GameLogEntry::plainText)
                .filter(text -> text.endsWith(" draws a card.") || text.contains(" discards "))
                .map(text -> text.endsWith(" draws a card.") ? "draw" : "discard")
                .toList();
        assertThat(drawAndDiscardEvents).containsExactly("draw", "draw", "discard", "discard");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void multipleDevilsAttackingTheSamePlayerDrawOnlyOnce() {
        Permanent zurzoth = addCreatureReady(player1, new ZurzothChaosRider());
        createDevilToken();
        Permanent devil = findPermanent(player1, "Devil");
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zurzoth),
                gd.playerBattlefields.get(player1.getId()).indexOf(devil)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Devil")).hasSize(3);
    }

    @Test
    void createdDevilDealsOneDamageToAPlayerWhenItDies() {
        Permanent devil = createZurzothDevil();
        harness.setLife(player2, 20);
        devil.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Devil")).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    void createdDevilCanDealItsDeathDamageToACreature() {
        Permanent devil = createZurzothDevil();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        devil.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent createZurzothDevil() {
        harness.addToBattlefield(player1, new ZurzothChaosRider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        draw(player2);
        resolveAllTriggers();
        return findPermanent(player1, "Devil");
    }

    private void createDevilToken() {
        harness.castFromHand(player1, new DanceWithDevils(), "{3}{R}");
        harness.passBothPriorities();
        findPermanent(player1, "Devil").setSummoningSick(false);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
