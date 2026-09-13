package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SternProctor.class, WornPowerstone.class, GloriousAnthem.class, CoralMerfolk.class})
class SternProctorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target artifact to its owner's hand")
    void etbReturnsArtifact() {
        harness.addToBattlefield(player2, new WornPowerstone());
        UUID targetId = harness.getPermanentId(player2, "Worn Powerstone");
        castSternProctor(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Worn Powerstone");
        harness.assertInHand(player2, "Worn Powerstone");
        harness.assertOnBattlefield(player1, "Stern Proctor");
    }

    @Test
    @DisplayName("ETB returns a target enchantment to its owner's hand")
    void etbReturnsEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        castSternProctor(targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInHand(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("ETB returns a target permanent to its owner's hand when controlled by another player")
    void etbReturnsTargetToItsOwnerHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        castSternProctor(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Worn Powerstone");
        harness.assertNotInHand(player1, "Worn Powerstone");
        harness.assertInHand(player2, "Worn Powerstone");
    }

    @Test
    @DisplayName("ETB cannot target a creature")
    void etbRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new CoralMerfolk());
        UUID targetId = harness.getPermanentId(player2, "Coral Merfolk");
        harness.setHand(player1, List.of(new SternProctor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    private void castSternProctor(UUID targetId) {
        harness.setHand(player1, List.of(new SternProctor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0, List.of(targetId));
    }
}
