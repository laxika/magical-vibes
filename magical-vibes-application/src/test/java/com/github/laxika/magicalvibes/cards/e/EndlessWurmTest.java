package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndlessWurm.class, GloriousAnthem.class, CoralMerfolk.class})
class EndlessWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep without an enchantment sacrifices Endless Wurm without prompting")
    void upkeepWithoutEnchantmentSacrificesWurm() {
        harness.addToBattlefield(player1, new EndlessWurm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Endless Wurm");
        harness.assertInGraveyard(player1, "Endless Wurm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Endless Wurm does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EndlessWurm());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Endless Wurm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing an enchantment keeps Endless Wurm")
    void sacrificingEnchantmentKeepsWurm() {
        harness.addToBattlefield(player1, new EndlessWurm());
        harness.addToBattlefield(player1, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Glorious Anthem").getId());

        harness.assertOnBattlefield(player1, "Endless Wurm");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Declining to sacrifice an enchantment sacrifices Endless Wurm")
    void decliningSacrificesWurm() {
        harness.addToBattlefield(player1, new EndlessWurm());
        harness.addToBattlefield(player1, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Endless Wurm");
        harness.assertInGraveyard(player1, "Endless Wurm");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("An opponent's enchantment cannot pay Endless Wurm's upkeep cost")
    void opponentEnchantmentDoesNotPayUpkeepCost() {
        harness.addToBattlefield(player1, new EndlessWurm());
        harness.addToBattlefield(player2, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Endless Wurm");
        harness.assertInGraveyard(player1, "Endless Wurm");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EndlessWurm());
        var blocker = addCreatureReady(player2, new CoralMerfolk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 8
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
