package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.cards.s.SabretoothTiger;
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

@CardUsed({WordOfBlasting.class, GlacialWall.class, SabretoothTiger.class})
class WordOfBlastingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Wall and deals its mana value to the Wall's controller")
    void destroysWallAndDealsManaValueDamage() {
        harness.addToBattlefield(player2, new GlacialWall());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Glacial Wall");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glacial Wall");
        harness.assertInGraveyard(player2, "Glacial Wall");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Destroyed Wall cannot be regenerated")
    void wallCannotBeRegenerated() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new GlacialWall());
        wall.setRegenerationShield(1);
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Glacial Wall");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glacial Wall");
        harness.assertInGraveyard(player2, "Glacial Wall");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals damage even when the targeted Wall is indestructible")
    void dealsDamageWhenWallIsIndestructible() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new GlacialWall());
        wall.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glacial Wall");
        harness.assertNotInGraveyard(player2, "Glacial Wall");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.addToBattlefield(player2, new SabretoothTiger());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Sabretooth Tiger");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
