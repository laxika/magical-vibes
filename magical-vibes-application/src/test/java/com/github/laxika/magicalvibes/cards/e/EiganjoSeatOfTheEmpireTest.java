package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.ImperialSubduer;
import com.github.laxika.magicalvibes.cards.n.NaomiPillarOfOrder;
import com.github.laxika.magicalvibes.cards.n.NorikaYamazakiThePoet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EiganjoSeatOfTheEmpire.class, ImperialSubduer.class,
        NaomiPillarOfOrder.class, NorikaYamazakiThePoet.class})
class EiganjoSeatOfTheEmpireTest extends BaseCardTest {

    @Test
    @DisplayName("Adds white mana")
    void addsWhiteMana() {
        harness.addToBattlefield(player1, new EiganjoSeatOfTheEmpire());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel deals 4 damage to an attacking creature and is reduced by a legendary creature")
    void channelDealsDamageWithLegendaryCostReduction() {
        harness.addToBattlefield(player1, new NorikaYamazakiThePoet());

        Permanent attacker = addCreatureReady(player2, new ImperialSubduer());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new EiganjoSeatOfTheEmpire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Imperial Subduer");
        harness.assertInGraveyard(player1, "Eiganjo, Seat of the Empire");
    }

    @Test
    @DisplayName("Channel cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ImperialSubduer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new EiganjoSeatOfTheEmpire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    void channelDealsFourDamageToBlockingCreatureAtFullCost() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NaomiPillarOfOrder());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new EiganjoSeatOfTheEmpire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, blocker.getId());

        harness.assertInGraveyard(player1, "Eiganjo, Seat of the Empire");
        harness.assertNotInHand(player1, "Eiganjo, Seat of the Empire");
        harness.assertOnBattlefield(player2, "Naomi, Pillar of Order");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Naomi, Pillar of Order");
        harness.assertInGraveyard(player2, "Naomi, Pillar of Order");
    }

    @Test
    void legendaryLandAndOpposingLegendaryCreatureDoNotReduceCost() {
        harness.addToBattlefield(player1, new EiganjoSeatOfTheEmpire());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new NorikaYamazakiThePoet());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new EiganjoSeatOfTheEmpire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Eiganjo, Seat of the Empire");
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Norika Yamazaki, the Poet");
    }

    @Test
    void twoLegendaryCreaturesReduceCostToOneWhiteMana() {
        harness.addToBattlefield(player1, new NorikaYamazakiThePoet());
        harness.addToBattlefield(player1, new NaomiPillarOfOrder());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ImperialSubduer());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new EiganjoSeatOfTheEmpire()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Eiganjo, Seat of the Empire");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Imperial Subduer");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void channelDoesNotResolveAgainstCreatureRemovedFromCombat() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ImperialSubduer());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new EiganjoSeatOfTheEmpire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Imperial Subduer");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Eiganjo, Seat of the Empire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelCanTargetItsControllersAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ImperialSubduer());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new EiganjoSeatOfTheEmpire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Imperial Subduer");
        harness.assertInGraveyard(player1, "Eiganjo, Seat of the Empire");
    }
}
