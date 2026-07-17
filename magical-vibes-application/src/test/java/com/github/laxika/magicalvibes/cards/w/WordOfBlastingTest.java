package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordOfBlastingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Wall and deals its mana value to the Wall's controller")
    void destroysWallAndDealsManaValueDamage() {
        harness.addToBattlefield(player2, new AngelicWall()); // {1}{W}, mana value 2
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Wall");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Wall");
        harness.assertInGraveyard(player2, "Angelic Wall");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Destroyed Wall cannot be regenerated")
    void wallCannotBeRegenerated() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        wall.setRegenerationShield(1);
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Wall");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Wall");
        harness.assertInGraveyard(player2, "Angelic Wall");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
