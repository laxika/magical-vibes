package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ExtusOriqOverlord.class, GrizzlyBears.class, Shock.class})
class ExtusOriqOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("Magecraft returns a nonlegendary creature card from the graveyard to hand")
    void magecraftReturnsNonlegendaryCreatureToHand() {
        harness.addToBattlefield(player1, new ExtusOriqOverlord());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Awaken the Blood Avatar sacrifices an opponent creature and creates the attacking Avatar token")
    void awakenTheBloodAvatarCreatesAvatarToken() {
        Permanent sacrificedForCost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorceryWithModesAndSacrifices(
                player1, 0, 1, 1, new int[]{1}, List.of(sacrificedForCost.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Avatar");
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 6);
    }
}
