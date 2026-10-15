package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mintstrosity.class, CandyGrapple.class})
class MintstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("When Mintstrosity dies, it creates a Food token")
    void deathCreatesFoodToken() {
        harness.addToBattlefield(player1, new Mintstrosity());
        UUID mintstrosityId = harness.getPermanentId(player1, "Mintstrosity");
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, mintstrosityId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertInGraveyard(player1, "Mintstrosity");
    }

    @Test
    @DisplayName("The Food token created by Mintstrosity can be sacrificed for 3 life")
    void foodCanBeSacrificedForLife() {
        harness.addToBattlefield(player1, new Mintstrosity());
        UUID mintstrosityId = harness.getPermanentId(player1, "Mintstrosity");
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, mintstrosityId);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Food is created only when the death trigger resolves")
    void foodCreationUsesTheStack() {
        harness.addToBattlefield(player1, new Mintstrosity());
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Mintstrosity"));

        harness.assertInGraveyard(player1, "Mintstrosity");
        harness.assertNotOnBattlefield(player1, "Food");

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("An opponent's Mintstrosity creates Food for that opponent")
    void opponentReceivesFoodAndLife() {
        harness.addToBattlefield(player2, new Mintstrosity());
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Mintstrosity"));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Food")).isOne();
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertInGraveyard(player2, "Mintstrosity");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Food");
    }
}
