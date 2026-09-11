package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaywardGuideBeast.class, Forest.class, GrizzlyBears.class})
class WaywardGuideBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes its controller return a land they control")
    void combatDamageReturnsControllersLand() {
        Permanent attacker = addCreatureReady(player1, new WaywardGuideBeast());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(ownLand.getId());

        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
