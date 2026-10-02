package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Phthisis;
import com.github.laxika.magicalvibes.cards.s.SuddenShock;
import com.github.laxika.magicalvibes.cards.u.UrborgSyphonMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ChildrenOfKorlis.class, Phthisis.class, SuddenShock.class, UrborgSyphonMage.class})
class ChildrenOfKorlisTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the life its controller lost this turn")
    void gainsLifeEqualToControllersLifeLostThisTurn() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChildrenOfKorlis());

        harness.setHand(player1, List.of(new SuddenShock(), new SuddenShock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Children of Korlis");
        harness.assertInGraveyard(player1, "Children of Korlis");
    }

    @Test
    @DisplayName("Counts direct life loss, not only damage")
    void gainsLifeEqualToDirectLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChildrenOfKorlis());
        harness.addToBattlefield(player1, new UrborgSyphonMage());

        harness.setHand(player1, List.of(new Phthisis()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, findPermanent(player1, "Urborg Syphon-Mage").getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Urborg Syphon-Mage");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Children of Korlis");
        harness.assertInGraveyard(player1, "Children of Korlis");
    }

    @Test
    @DisplayName("Gains no life when its controller lost no life this turn")
    void gainsNoLifeWhenControllerLostNoLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ChildrenOfKorlis());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Children of Korlis");
        harness.assertInGraveyard(player1, "Children of Korlis");
    }
}
