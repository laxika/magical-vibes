package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SamiteCenserBearer.class, FomoriNomad.class, ScourgeOfKherRidges.class})
class SamiteCenserBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it prevents the next damage to each creature you control")
    void sacrificesAndPreventsDamageToControlledCreatures() {
        addCreatureReady(player1, new SamiteCenserBearer());
        Permanent ownCreature = addCreatureReady(player1, new FomoriNomad());
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Samite Censer-Bearer");
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention does not affect an opponent's creature or creatures entering later")
    void onlyProtectsControlledCreaturesAtResolution() {
        addCreatureReady(player1, new SamiteCenserBearer());
        Permanent ownCreature = addCreatureReady(player1, new FomoriNomad());
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        Permanent opponentCreature = addCreatureReady(player2, new FomoriNomad());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());

        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(lateCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each controlled creature gets a separate shield for only its next damage")
    void shieldsEachControlledCreatureForOnlyNextDamage() {
        addCreatureReady(player1, new SamiteCenserBearer());
        Permanent firstCreature = addCreatureReady(player1, new FomoriNomad());
        Permanent secondCreature = addCreatureReady(player1, new FomoriNomad());
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();

        assertThat(firstCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(secondCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new SamiteCenserBearer());
        Permanent ownCreature = addCreatureReady(player1, new FomoriNomad());
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The shield is consumed and does not reduce a second damage event")
    void subsequentDamageIsNotPrevented() {
        addCreatureReady(player1, new SamiteCenserBearer());
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isEqualTo(1);

        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature entering before the ability resolves receives a shield")
    void protectsCreaturesEnteringBeforeResolution() {
        harness.addToBattlefield(player1, new SamiteCenserBearer());
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FomoriNomad());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two activations provide independent shields that together prevent two damage")
    void multipleActivationsAccumulatePrevention() {
        addCreatureReady(player1, new SamiteCenserBearer());
        addCreatureReady(player1, new SamiteCenserBearer());
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        Permanent scourge = addCreatureReady(player1, new ScourgeOfKherRidges());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isZero();

        harness.activateAbility(player1, indexOf(player1, scourge), null, null);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
