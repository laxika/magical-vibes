package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreaterHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, its controller chooses a permanent to sacrifice")
    void upkeepSacrificesAControlledPermanent() {
        harness.addToBattlefield(player1, new GreaterHarvester());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent forest = findPermanent(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Greater Harvester");
    }

    @Test
    @DisplayName("Combat damage makes the damaged player sacrifice two permanents")
    void combatDamageMakesDamagedPlayerSacrificeTwoPermanents() {
        Permanent harvester = addCreatureReady(player1, new GreaterHarvester());
        harvester.setAttacking(true);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        harness.handleMultiplePermanentsChosen(player2,
                List.of(findPermanent(player2, "Forest").getId(), findPermanent(player2, "Mountain").getId()));

        harness.assertNotOnBattlefield(player2, "Mountain");
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }
}
