package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoadkillRodney.class, GrizzlyBears.class})
class RoadkillRodneyTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new RoadkillRodney()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{3}", "{3}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Roadkill Rodney")).hasSize(3);
        assertThat(findPermanents(player1, "Roadkill Rodney"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Combat damage to a player creates a Mutagen token")
    void combatDamageCreatesMutagen() {
        Permanent rodney = addCreatureReady(player1, new RoadkillRodney());
        rodney.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Blocked combat damage does not create a Mutagen token")
    void blockedCombatDoesNotCreateMutagen() {
        Permanent rodney = addCreatureReady(player1, new RoadkillRodney());
        rodney.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }
}
