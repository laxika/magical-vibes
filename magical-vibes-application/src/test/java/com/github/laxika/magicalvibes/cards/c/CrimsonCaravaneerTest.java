package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimsonCaravaneer.class, GrizzlyBears.class})
class CrimsonCaravaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Junk token for each combat damage step that hits a player")
    void createsJunkForEachCombatDamageStep() {
        Permanent caravaneer = addCreatureReady(player1, new CrimsonCaravaneer());
        caravaneer.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Junk")).hasSize(2);
    }

    @Test
    @DisplayName("Does not create Junk when blocked")
    void doesNotCreateJunkWhenBlocked() {
        Permanent caravaneer = addCreatureReady(player1, new CrimsonCaravaneer());
        caravaneer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Junk")).isEmpty();
    }
}
