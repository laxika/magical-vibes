package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RequiemMonolith.class, GrizzlyBears.class, Shock.class})
class RequiemMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's controller may have the Monolith deal damage and then draws and loses that much")
    void targetControllerAcceptsDamage() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted ability triggers for lethal damage and uses the total damage dealt")
    void lethalDamageTriggersDrawAndLifeLoss() {
        harness.addToBattlefield(player1, new RequiemMonolith());
        Permanent target = addCreatureReady(player2);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
