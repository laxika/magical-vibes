package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DanceWithDevils;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

    private void createDevilToken() {
        harness.setHand(player1, List.of(new DanceWithDevils()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Devil").setSummoningSick(false);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
