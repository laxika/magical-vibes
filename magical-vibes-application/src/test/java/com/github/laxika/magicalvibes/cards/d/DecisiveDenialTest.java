package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecisiveDenial.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class})
class DecisiveDenialTest extends BaseCardTest {

    @Test
    void fightModeDealsNoDamageWhenOneCreatureLeavesBeforeResolution() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        harness.setGraveyard(player1, List.of(ownCreature.getCard()));
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Decisive Denial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fightModeKillsBothCreaturesWhenEachDealsLethalDamage() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void fightModeRejectsASecondTargetYouControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterModeCountersWhenTheControllerDeclinesAnAffordablePayment() {
        GiantGrowth growth = new GiantGrowth();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(growth));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new DecisiveDenial()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 1, growth.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
    }

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
