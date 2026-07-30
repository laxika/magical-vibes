package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NefaroxOverlordOfGrixisTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone makes the defending player sacrifice their lone creature")
    void attacksAloneForcesDefenderSacrifice() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        harness.addToBattlefield(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("The defending player chooses which creature to sacrifice")
    void defenderChoosesSacrifice() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        // Two triggers go on the stack: exalted and the attack edict.
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, hawk.getId());

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking together with another creature does not force a sacrifice")
    void noSacrificeWhenNotAlone() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void exaltedBoostsLoneAlly() {
        addCreatureReady(player1, new NefaroxOverlordOfGrixis());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — Nefarox attacking alone boosts itself")
    void exaltedBoostsSelf() {
        Permanent nefarox = addCreatureReady(player1, new NefaroxOverlordOfGrixis());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nefarox)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, nefarox)).isEqualTo(6);
    }
}
