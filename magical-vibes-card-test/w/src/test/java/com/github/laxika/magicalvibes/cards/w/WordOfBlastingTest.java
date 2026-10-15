package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlasterMage;
import com.github.laxika.magicalvibes.cards.c.CrenellatedWall;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WordOfBlasting.class, CrenellatedWall.class, BlasterMage.class, Unsummon.class})
class WordOfBlastingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Wall and deals its mana value to the Wall's controller")
    void destroysWallAndDealsManaValueDamage() {
        harness.addToBattlefield(player2, new CrenellatedWall());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Crenellated Wall");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Crenellated Wall");
        harness.assertInGraveyard(player2, "Crenellated Wall");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Destroyed Wall cannot be regenerated")
    void wallCannotBeRegenerated() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new CrenellatedWall());
        wall.setRegenerationShield(1);
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Crenellated Wall");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Crenellated Wall");
        harness.assertInGraveyard(player2, "Crenellated Wall");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Deals damage even when the targeted Wall is indestructible")
    void dealsDamageWhenWallIsIndestructible() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new CrenellatedWall());
        wall.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, wall.getId());

        harness.assertOnBattlefield(player2, "Crenellated Wall");
        harness.assertNotInGraveyard(player2, "Crenellated Wall");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.addToBattlefield(player2, new BlasterMage());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Blaster Mage");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy its controller's own Wall and damages that player")
    void destroysOwnWallAndDamagesItsController() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new CrenellatedWall());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, wall.getId());

        harness.assertNotOnBattlefield(player1, "Crenellated Wall");
        harness.assertInGraveyard(player1, "Crenellated Wall");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals no damage if the Wall leaves the battlefield before resolution")
    void noDamageWhenWallIsReturnedToHand() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new CrenellatedWall());
        harness.setHand(player1, List.of(new WordOfBlasting(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, wall.getId());
        harness.castAndResolveInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Crenellated Wall");
        harness.assertNotInGraveyard(player2, "Crenellated Wall");
        harness.assertInGraveyard(player1, "Word of Blasting");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
