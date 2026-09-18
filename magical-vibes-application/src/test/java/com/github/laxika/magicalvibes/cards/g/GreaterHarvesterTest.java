package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreaterHarvester.class, DarksteelCitadel.class})
class GreaterHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, its controller chooses a permanent to sacrifice")
    void upkeepSacrificesAControlledPermanent() {
        harness.addToBattlefield(player1, new GreaterHarvester());
        harness.addToBattlefield(player1, new DarksteelCitadel());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        Permanent harvester = findPermanent(player1, "Greater Harvester");
        Permanent citadel = findPermanent(player1, "Darksteel Citadel");
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(harvester.getId(), citadel.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(citadel.getId()));

        harness.assertNotOnBattlefield(player1, "Darksteel Citadel");
        harness.assertOnBattlefield(player1, "Greater Harvester");
    }

    @Test
    @DisplayName("Combat damage makes the damaged player sacrifice two permanents")
    void combatDamageMakesDamagedPlayerSacrificeTwoPermanents() {
        Permanent harvester = addCreatureReady(player1, new GreaterHarvester());
        harvester.setAttacking(true);
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        List<Permanent> citadels = findPermanents(player2, "Darksteel Citadel");
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                citadels.stream().map(Permanent::getId).toList());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        harness.handleMultiplePermanentsChosen(player2,
                List.of(citadels.get(0).getId(), citadels.get(1).getId()));

        assertThat(countPermanents(player2, "Darksteel Citadel")).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage trigger does not fire when the creature deals no damage to a player")
    void combatDamageTriggerDoesNotFireWhenBlocked() {
        Permanent harvester = addCreatureReady(player1, new GreaterHarvester());
        harvester.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GreaterHarvester());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player2, "Greater Harvester")).isEqualTo(1);
    }
}
