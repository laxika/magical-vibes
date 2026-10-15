package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AgentMariaHill;
import com.github.laxika.magicalvibes.cards.h.HydraulicHelper;
import com.github.laxika.magicalvibes.cards.h.HourOfDefeat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainAmericaWingsOfFreedom.class, AgentMariaHill.class, HydraulicHelper.class,
        CaptainAmericasShield.class, HourOfDefeat.class})
class CaptainAmericaWingsOfFreedomTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other Heroes you control by Captain America's toughness")
    void attackBoostsOtherHeroesYouControl() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());
        Permanent ownNonHero = addCreatureReady(player1, new HydraulicHelper());
        Permanent opponentHero = addCreatureReady(player2, new AgentMariaHill());
        captain.setToughnessModifier(2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonHero)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHero)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(2);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost uses Captain America's toughness when the trigger resolves")
    void usesToughnessAtResolution() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());

        declareAttackers(List.of(0));
        captain.setToughnessModifier(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost includes toughness granted by Equipment")
    void includesEquipmentToughness() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new CaptainAmericasShield());
        shield.setAttachedTo(captain.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(10);
    }

    @Test
    @DisplayName("The boost uses equipped Captain America's last known toughness after he dies")
    void usesLastKnownEquipmentToughnessAfterSourceDies() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());
        Permanent ownHero = addCreatureReady(player1, new AgentMariaHill());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new CaptainAmericasShield());
        shield.setAttachedTo(captain.getId());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HourOfDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0, captain.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(captain.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownHero)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, ownHero)).isEqualTo(10);
    }

    @Test
    @DisplayName("Heroes entering after resolution do not receive the boost")
    void doesNotBoostHeroesEnteringAfterResolution() {
        addCreatureReady(player1, new CaptainAmericaWingsOfFreedom());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        Permanent lateHero = addCreatureReady(player1, new AgentMariaHill());

        assertThat(gqs.getEffectivePower(gd, lateHero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateHero)).isEqualTo(1);
    }
}
