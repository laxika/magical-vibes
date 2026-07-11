package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LurkingNightstalker;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnOfTheNightstalkersTest extends BaseCardTest {

    private void castReturnOfTheNightstalkers() {
        harness.setHand(player1, new ArrayList<>(List.of(new ReturnOfTheNightstalkers())));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
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

        harness.assertOnBattlefield(player1, "Lurking Nightstalker");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nightstalker);
    }

    @Test
    @DisplayName("Does not return non-Nightstalker cards from graveyard")
    void doesNotReturnNonNightstalkers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(creature);

        castReturnOfTheNightstalkers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Destroys all Swamps the controller controls")
    void destroysControllerSwamps() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        castReturnOfTheNightstalkers();

        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Does not destroy opponent's Swamps or the controller's non-Swamp lands")
    void leavesOtherLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Swamp());

        castReturnOfTheNightstalkers();

        harness.assertNotOnBattlefield(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Swamp");
    }

    @Test
    @DisplayName("Returns Nightstalkers and destroys Swamps in the same resolution")
    void returnsAndDestroysTogether() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card nightstalker = new LurkingNightstalker();
        gd.playerGraveyards.get(player1.getId()).add(nightstalker);
        harness.addToBattlefield(player1, new Swamp());

        castReturnOfTheNightstalkers();

        harness.assertOnBattlefield(player1, "Lurking Nightstalker");
        harness.assertNotOnBattlefield(player1, "Swamp");
    }
}
