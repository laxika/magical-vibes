package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class PurifyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys both artifacts and enchantments")
    void destroysArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.setHand(player1, List.of(new Purify()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertInGraveyard(player1, "Pithing Needle");
        harness.assertInGraveyard(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Purify()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Purify goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Purify()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Purify");
    }
}
