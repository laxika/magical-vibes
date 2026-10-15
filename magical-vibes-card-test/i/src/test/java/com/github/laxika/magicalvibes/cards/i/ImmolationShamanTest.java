package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.s.SimicLocket;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImmolationShaman.class, IncubationDruid.class, SimicLocket.class, DovinGrandArbiter.class})
class ImmolationShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent activating an artifact, creature, or land's non-mana ability deals 1 damage")
    void opponentNonManaAbilityDealsDamage() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        harness.addToBattlefield(player2, new IncubationDruid());
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A mana ability does not trigger Immolation Shaman")
    void manaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        harness.addToBattlefield(player2, new SimicLocket());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Activated ability gives Immolation Shaman +3/+3 and menace until end of turn")
    void activatedAbilityBoostsAndGrantsMenace() {
        Permanent shaman = addReadyShaman();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, shaman, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Immolation Shaman's activated ability wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent shaman = addReadyShaman();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, shaman, Keyword.MENACE)).isFalse();
    }

    @Test
    void adaptTriggersBeforeOpponentsCreatureAbilityResolves() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new IncubationDruid());
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(gqs.getEffectivePower(gd, druid)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, druid)).isEqualTo(3);
        harness.assertLife(player2, 19);
    }

    @Test
    void sacrificingArtifactAsActivationCostStillTriggers() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        harness.addToBattlefield(player2, new SimicLocket());
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.setLibrary(player2, List.of(new ImmolationShaman(), new ImmolationShaman()));
        harness.setLife(player2, 20);
        int handSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, 1, null, null);

        harness.assertNotOnBattlefield(player2, "Simic Locket");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 2);
        harness.assertLife(player2, 19);
    }

    @Test
    void artifactManaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        harness.addToBattlefield(player2, new SimicLocket());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void controllersOwnActivationDoesNotTrigger() {
        addReadyShaman();
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void planeswalkerAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        Permanent dovin = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        dovin.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    void repeatedActivationsStackBoosts() {
        Permanent shaman = addReadyShaman();
        harness.addMana(player1, ManaColor.RED, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, shaman, Keyword.MENACE)).isTrue();
    }

    private Permanent addReadyShaman() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new ImmolationShaman());
        shaman.setSummoningSick(false);
        return shaman;
    }

}
