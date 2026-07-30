package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class AngelOfGlorysRiseTest extends BaseCardTest {

    private void castAngel() {
        harness.setHand(player1, new ArrayList<>(List.of(new AngelOfGlorysRise())));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("ETB exiles all Zombies on the battlefield, regardless of controller")
    void etbExilesAllZombies() {
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.addToBattlefield(player2, new DiregrafGhoul());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAngel();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player2, "Diregraf Ghoul");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB returns all Human creature cards from your graveyard to the battlefield")
    void etbReturnsHumansFromOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new EliteVanguard(), new SavannahLions()));

        castAngel();

        harness.assertOnBattlefield(player1, "Elite Vanguard");
        harness.assertNotInGraveyard(player1, "Elite Vanguard");
        harness.assertInGraveyard(player1, "Savannah Lions");
    }

    @Test
    @DisplayName("Humans in an opponent's graveyard are not returned")
    void opponentHumansStayInGraveyard() {
        harness.setGraveyard(player2, List.of(new EliteVanguard()));

        castAngel();

        harness.assertInGraveyard(player2, "Elite Vanguard");
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Resolves with no Zombies and an empty graveyard")
    void resolvesWithNothingToDo() {
        castAngel();

        harness.assertOnBattlefield(player1, "Angel of Glory's Rise");
    }
}
