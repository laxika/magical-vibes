package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Serenity.class, MindStone.class, SerrasBlessing.class, BenalishInfantry.class})
class SerenityTest extends BaseCardTest {

    private void advanceToUpkeepAndResolveTrigger(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities(); // resolve the upkeep trigger
    }

    @Test
    @DisplayName("Destroys all artifacts and enchantments on controller's upkeep, including itself")
    void destroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new Serenity());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player2, new SerrasBlessing());

        advanceToUpkeepAndResolveTrigger(player1);

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Serenity");
        harness.assertNotOnBattlefield(player2, "Serra's Blessing");
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Serenity");
        harness.assertInGraveyard(player2, "Serra's Blessing");
    }

    @Test
    @DisplayName("Artifacts and enchantments cannot regenerate from Serenity")
    void artifactsAndEnchantmentsCannotRegenerate() {
        harness.addToBattlefield(player1, new Serenity());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        artifact.setRegenerationShield(1);

        advanceToUpkeepAndResolveTrigger(player1);

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        harness.assertInGraveyard(player2, "Mind Stone");
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
