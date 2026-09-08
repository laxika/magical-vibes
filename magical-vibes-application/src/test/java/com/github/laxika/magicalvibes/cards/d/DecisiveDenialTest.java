package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecisiveDenialTest extends BaseCardTest {

    @Test
    void fightModeMakesTheTwoCreaturesDealDamageToEachOther() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Decisive Denial");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void fightModeRejectsAFirstTargetYouDoNotControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, 0, List.of(opposingCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterModeCountersANoncreatureSpellWhenItsControllerCannotPay() {
        GiantGrowth growth = new GiantGrowth();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(growth));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, ownCreature.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 1, growth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void counterModeRejectsACreatureSpell() {
        GrizzlyBears creatureSpell = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(creatureSpell));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, 1, List.of(creatureSpell.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterModeLeavesTheSpellOnTheStackWhenItsControllerPays() {
        GiantGrowth growth = new GiantGrowth();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(growth));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, ownCreature.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 1, growth.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getEffectivePower()).isEqualTo(5);
        harness.assertInGraveyard(player2, "Giant Growth");
    }
}
