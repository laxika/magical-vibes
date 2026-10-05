package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RabidGnaw.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class RabidGnawTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the creature and deals its boosted power as damage")
    void boostsAndDealsPowerDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        cast();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("A creature survives when its toughness is greater than the boosted power")
    void higherToughnessCreatureSurvives() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        cast();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The first target must be a creature controlled by the caster")
    void firstTargetMustBeControlled() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RabidGnaw()));
        addMana();

        UUID firstTarget = harness.getPermanentId(player2, "Grizzly Bears");
        UUID secondTarget = harness.getPermanentId(player2, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstTarget, secondTarget)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The second target must be a creature an opponent controls")
    void secondTargetMustBeOpponentsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new RabidGnaw()));
        addMana();

        UUID firstTarget = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondTarget = harness.getPermanentId(player1, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstTarget, secondTarget)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost still applies when the damage target is removed before resolution")
    void boostAppliesIfSecondTargetIsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RabidGnaw()));
        addMana();

        UUID firstTarget = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondTarget = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(firstTarget, secondTarget));
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals exactly the boosted power without dealing damage back to the source")
    void dealsExactBoostedPowerWithoutReciprocalDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast();

        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("No damage is dealt when the source creature leaves before resolution")
    void noDamageIfFirstTargetIsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new RabidGnaw()));
        addMana();
        harness.castInstant(player1, 0, List.of(harness.getPermanentId(player1, "Grizzly Bears"), elemental.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(elemental.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Rabid Gnaw");
    }

    @Test
    @DisplayName("A source that changes to the opponent's control receives no boost and deals no damage")
    void noDamageIfFirstTargetChangesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new RabidGnaw()));
        addMana();
        harness.castInstant(player1, 0, List.of(bear.getId(), elemental.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(elemental.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The boost applies but damage does not when the victim changes to the caster's control")
    void boostAppliesIfSecondTargetChangesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new RabidGnaw()));
        addMana();
        harness.castInstant(player1, 0, List.of(bear.getId(), elemental.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(elemental);
        gd.playerBattlefields.get(player1.getId()).add(elemental);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(elemental.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The power boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        cast();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    private void cast() {
        harness.setHand(player1, List.of(new RabidGnaw()));
        addMana();
        UUID firstTarget = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondTarget = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst().getId();
        harness.castAndResolveInstant(player1, 0, List.of(firstTarget, secondTarget));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
