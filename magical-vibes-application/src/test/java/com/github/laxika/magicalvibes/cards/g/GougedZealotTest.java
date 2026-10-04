package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AbundantHarvest;
import com.github.laxika.magicalvibes.cards.d.DakkonShadowSlayer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.u.UnholyHeat;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GougedZealot.class, GrizzlyBears.class, Forest.class, Shock.class, Pacifism.class,
        OrnithopterOfParadise.class, UnholyHeat.class, AbundantHarvest.class, DakkonShadowSlayer.class})
class GougedZealotTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, attacking deals 1 damage to each defending creature")
    void attacksDamageDefendingCreaturesWithDelirium() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        setDelirium();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Without delirium, attacking does not damage defending creatures")
    void attacksDoNotDamageDefendingCreaturesWithoutDelirium() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An artifact creature contributes two card types to delirium")
    void multipleTypesOnOneCardEnableDelirium() {
        addCreatureReady(player1, new GougedZealot());
        Permanent firstDefender = addCreatureReady(player2, new OrnithopterOfParadise());
        Permanent secondDefender = addCreatureReady(player2, new GougedZealot());
        harness.setGraveyard(player1, List.of(
                new OrnithopterOfParadise(), new UnholyHeat(), new AbundantHarvest()));

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(firstDefender.getMarkedDamage()).isEqualTo(1);
        assertThat(secondDefender.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Losing delirium before resolution prevents the attack damage")
    void losingDeliriumBeforeResolutionPreventsDamage() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defender = addCreatureReady(player2, new OrnithopterOfParadise());
        harness.setGraveyard(player1, List.of(
                new OrnithopterOfParadise(), new UnholyHeat(), new AbundantHarvest()));

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new OrnithopterOfParadise(), new UnholyHeat()));
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Gaining delirium after attacking does not create a trigger")
    void gainingDeliriumAfterAttackingDoesNotTrigger() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defender = addCreatureReady(player2, new OrnithopterOfParadise());
        harness.setGraveyard(player1, List.of(new OrnithopterOfParadise(), new UnholyHeat()));

        declareAttackers(List.of(0));
        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1, List.of(
                new OrnithopterOfParadise(), new UnholyHeat(), new AbundantHarvest()));
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Card types in the opponent's graveyard do not enable delirium")
    void opponentsGraveyardDoesNotEnableDelirium() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defender = addCreatureReady(player2, new OrnithopterOfParadise());
        harness.setGraveyard(player2, List.of(
                new OrnithopterOfParadise(), new UnholyHeat(), new AbundantHarvest()));

        declareAttackers(List.of(0));
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removing the attacked planeswalker does not prevent damage to its controller's creatures")
    void damageStillOccursAfterAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new GougedZealot());
        Permanent defender = addCreatureReady(player2, new OrnithopterOfParadise());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DakkonShadowSlayer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setGraveyard(player1, List.of(
                new OrnithopterOfParadise(), new UnholyHeat(), new AbundantHarvest()));
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());
        harness.assertNotOnBattlefield(player2, "Dakkon, Shadow Slayer");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }
}
