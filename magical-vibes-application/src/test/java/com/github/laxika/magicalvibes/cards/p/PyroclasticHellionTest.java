package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyroclasticHellion.class, Mountain.class, GrizzlyBears.class})
class PyroclasticHellionTest extends BaseCardTest {

    @Test
    @DisplayName("Returning a land makes Pyroclastic Hellion deal 2 damage to each opponent")
    void returningLandDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new Mountain());
        int lifeBefore = gd.getLife(player2.getId());
        castAndResolveHellion();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mountain"));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertOnBattlefield(player1, "Pyroclastic Hellion");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining the return does not deal damage")
    void decliningReturnDealsNoDamage() {
        harness.addToBattlefield(player1, new Mountain());
        int lifeBefore = gd.getLife(player2.getId());
        castAndResolveHellion();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("The return choice only offers lands you control")
    void returnChoiceOnlyOffersOwnLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());
        UUID ownMountainId = harness.getPermanentId(player1, "Mountain");
        castAndResolveHellion();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownMountainId);
    }

    private void castAndResolveHellion() {
        harness.setHand(player1, List.of(new PyroclasticHellion()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
