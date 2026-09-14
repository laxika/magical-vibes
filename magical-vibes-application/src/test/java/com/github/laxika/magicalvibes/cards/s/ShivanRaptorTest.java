package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShivanRaptor.class, GorillaWarrior.class})
class ShivanRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Shivan Raptor to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        castAndResolveShivanRaptor();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("First strike destroys a blocker before it can deal combat damage")
    void firstStrikeDealsDamageBeforeBlocker() {
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());
        Permanent raptor = addCreatureReady(player1, new ShivanRaptor());
        raptor.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(raptor);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shivan Raptor");
        harness.assertInGraveyard(player2, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Declining echo sacrifices Shivan Raptor at its next upkeep")
    void decliningEchoSacrificesShivanRaptor() {
        castAndResolveShivanRaptor();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Shivan Raptor");
        harness.assertInGraveyard(player1, "Shivan Raptor");
    }

    @Test
    @DisplayName("Echo does not trigger during the opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveShivanRaptor();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Shivan Raptor");
    }

    @Test
    @DisplayName("Paying echo keeps Shivan Raptor and echo does not trigger again")
    void payingEchoKeepsShivanRaptorAndIsOneShot() {
        castAndResolveShivanRaptor();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Shivan Raptor");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Shivan Raptor");
    }

    private void castAndResolveShivanRaptor() {
        harness.castFromHand(player1, new ShivanRaptor(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shivan Raptor");
    }
}
