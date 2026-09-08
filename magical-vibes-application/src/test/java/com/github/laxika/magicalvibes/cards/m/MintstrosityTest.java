package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mintstrosity.class, LightningBolt.class})
class MintstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("When Mintstrosity dies, it creates a Food token")
    void deathCreatesFoodToken() {
        harness.addToBattlefield(player1, new Mintstrosity());
        UUID mintstrosityId = harness.getPermanentId(player1, "Mintstrosity");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, mintstrosityId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertInGraveyard(player1, "Mintstrosity");
    }

    @Test
    @DisplayName("The Food token created by Mintstrosity can be sacrificed for 3 life")
    void foodCanBeSacrificedForLife() {
        harness.addToBattlefield(player1, new Mintstrosity());
        UUID mintstrosityId = harness.getPermanentId(player1, "Mintstrosity");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, mintstrosityId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }
}
