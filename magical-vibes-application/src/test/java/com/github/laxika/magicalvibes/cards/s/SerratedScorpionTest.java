package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerratedScorpion.class})
class SerratedScorpionTest extends BaseCardTest {

    @Test
    void whenItDiesItDamagesEachOpponentAndGainsLife() {
        Permanent scorpion = harness.addToBattlefieldAndReturn(player1, new SerratedScorpion());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, scorpion));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Serrated Scorpion");
    }

    @Test
    void deathAbilityUsesTheDyingCreaturesController() {
        Permanent scorpion = harness.addToBattlefieldAndReturn(player2, new SerratedScorpion());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, scorpion));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 12);
        harness.assertInGraveyard(player2, "Serrated Scorpion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exileDoesNotTriggerTheDeathAbility() {
        Permanent scorpion = harness.addToBattlefieldAndReturn(player1, new SerratedScorpion());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, scorpion));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Serrated Scorpion");
        harness.assertNotInGraveyard(player1, "Serrated Scorpion");
        assertThat(gd.exiledCards).hasSize(1);
    }
}
