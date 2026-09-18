package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerendibDjinn.class, Island.class, Mountain.class})
class SerendibDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island at upkeep deals 3 damage to its controller")
    void sacrificingIslandDealsDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Sacrificing a non-Island land deals no damage")
    void sacrificingNonIslandDealsNoDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Controller chooses which land to sacrifice")
    void controllerChoosesLand() {
        harness.setLife(player1, 20);
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactly(island.getId(), mountain.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(mountain.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Sacrifices itself when its controller controls no lands")
    void sacrificesWhenControllerHasNoLands() {
        harness.addToBattlefield(player1, new SerendibDjinn());

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Serendib Djinn");
    }
}
