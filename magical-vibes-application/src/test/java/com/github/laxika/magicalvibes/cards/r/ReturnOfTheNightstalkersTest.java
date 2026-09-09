package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.e.EyeSpy;
import com.github.laxika.magicalvibes.cards.l.LurkingNightstalker;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReturnOfTheNightstalkers.class, LurkingNightstalker.class, AlabornTrooper.class,
        EyeSpy.class, Mountain.class, Swamp.class})
class ReturnOfTheNightstalkersTest extends BaseCardTest {

    private void castReturnOfTheNightstalkers() {
        harness.castFromHand(player1, new ReturnOfTheNightstalkers(), "{5}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns Nightstalker cards from controller's graveyard to the battlefield")
    void returnsNightstalkersFromGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card nightstalker = new LurkingNightstalker();
        gd.playerGraveyards.get(player1.getId()).add(nightstalker);

        castReturnOfTheNightstalkers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == nightstalker);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nightstalker);
    }

    @Test
    @DisplayName("Does not return non-Nightstalker cards from graveyard")
    void doesNotReturnNonNightstalkers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new AlabornTrooper();
        gd.playerGraveyards.get(player1.getId()).add(creature);

        castReturnOfTheNightstalkers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Does not return nonpermanent cards with the Nightstalker subtype")
    void doesNotReturnNonPermanentNightstalkers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card nonPermanentNightstalker = new EyeSpy();
        nonPermanentNightstalker.setSubtypes(List.of(CardSubtype.NIGHTSTALKER));
        gd.playerGraveyards.get(player1.getId()).add(nonPermanentNightstalker);

        castReturnOfTheNightstalkers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == nonPermanentNightstalker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonPermanentNightstalker);
    }

    @Test
    @DisplayName("Destroys all Swamps the controller controls")
    void destroysControllerSwamps() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent firstSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent secondSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        castReturnOfTheNightstalkers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent == firstSwamp || permanent == secondSwamp);
    }

    @Test
    @DisplayName("Does not destroy opponent's Swamps or the controller's non-Swamp lands")
    void leavesOtherLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent ownSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent ownMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opponentSwamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        castReturnOfTheNightstalkers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownSwamp);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownMountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentSwamp);
    }

    @Test
    @DisplayName("Returns Nightstalkers and destroys Swamps in the same resolution")
    void returnsAndDestroysTogether() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card nightstalker = new LurkingNightstalker();
        gd.playerGraveyards.get(player1.getId()).add(nightstalker);
        Permanent ownSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        castReturnOfTheNightstalkers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == nightstalker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownSwamp);
    }
}
