package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionWhite;
import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.w.WoodenSphere;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({BenalishInfantry.class, CircleOfProtectionWhite.class, DancingScimitar.class, Serenity.class, SerrasBlessing.class, WoodenSphere.class})
class SerenityTest extends BaseCardTest {

    private void advanceToUpkeepAndResolveTrigger(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities(); // resolve the upkeep trigger
    }

    @Test
    @DisplayName("Destroys all artifacts and enchantments on controller's upkeep, including itself")
    void destroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new Serenity());
        harness.addToBattlefield(player1, new WoodenSphere());
        harness.addToBattlefield(player2, new CircleOfProtectionWhite());

        advanceToUpkeepAndResolveTrigger(player1);

        harness.assertNotOnBattlefield(player1, "Wooden Sphere");
        harness.assertNotOnBattlefield(player1, "Serenity");
        harness.assertNotOnBattlefield(player2, "Circle of Protection: White");
        harness.assertInGraveyard(player1, "Wooden Sphere");
        harness.assertInGraveyard(player1, "Serenity");
        harness.assertInGraveyard(player2, "Circle of Protection: White");
    }

    @Test
    @DisplayName("Destroys artifact creatures even when they have regeneration shields")
    void cannotBeRegenerated() {
        harness.addToBattlefield(player1, new Serenity());
        var scimitar = harness.addToBattlefieldAndReturn(player2, new DancingScimitar());
        scimitar.setRegenerationShield(1);

        advanceToUpkeepAndResolveTrigger(player1);

        harness.assertNotOnBattlefield(player2, "Dancing Scimitar");
        harness.assertInGraveyard(player2, "Dancing Scimitar");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new Serenity());
        harness.addToBattlefield(player1, new BenalishInfantry());

        advanceToUpkeepAndResolveTrigger(player1);

        harness.assertOnBattlefield(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Serenity());
        harness.addToBattlefield(player2, new SerrasBlessing());

        advanceToUpkeepAndResolveTrigger(player2);

        harness.assertOnBattlefield(player2, "Serra's Blessing");
        harness.assertOnBattlefield(player1, "Serenity");
    }
}
