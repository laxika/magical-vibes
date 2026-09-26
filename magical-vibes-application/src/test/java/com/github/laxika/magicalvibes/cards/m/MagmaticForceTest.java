package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagmaticForce.class, GrizzlyBears.class})
class MagmaticForceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a chosen player at the beginning of each upkeep")
    void dealsDamageToChosenPlayerAtEachUpkeep() {
        harness.addToBattlefield(player1, new MagmaticForce());
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to a chosen creature at the beginning of upkeep")
    void dealsDamageToChosenCreature() {
        harness.addToBattlefield(player1, new MagmaticForce());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
    }
}
