package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SerenityTest extends BaseCardTest {

    private void advanceToUpkeepAndResolveTrigger(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities(); // resolve the upkeep trigger
    }

    @Test
    @DisplayName("Destroys all artifacts and enchantments on controller's upkeep, including itself")
    void destroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new Serenity());
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player2, new RuleOfLaw());

        advanceToUpkeepAndResolveTrigger(player1);

        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        harness.assertNotOnBattlefield(player1, "Serenity");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertInGraveyard(player1, "Pithing Needle");
        harness.assertInGraveyard(player1, "Serenity");
        harness.assertInGraveyard(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new Serenity());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeepAndResolveTrigger(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Serenity());
        harness.addToBattlefield(player2, new RuleOfLaw());

        advanceToUpkeepAndResolveTrigger(player2);

        harness.assertOnBattlefield(player2, "Rule of Law");
        harness.assertOnBattlefield(player1, "Serenity");
    }
}
