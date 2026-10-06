package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwaterAlligator.class, Forest.class, CityOfTraitors.class})
class RootwaterAlligatorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest grants Rootwater Alligator a regeneration shield")
    void sacrificingForestRegeneratesRootwaterAlligator() {
        Permanent alligator = harness.addToBattlefieldAndReturn(player1, new RootwaterAlligator());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(alligator.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller chooses which Forest to sacrifice when multiple are available")
    void choosesForestWhenMultipleAreAvailable() {
        Permanent alligator = addCreatureReady(player1, new RootwaterAlligator());
        Permanent forestToSacrifice = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent remainingForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forestToSacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alligator, remainingForest);
        assertThat(alligator.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("A regeneration shield saves Rootwater Alligator from lethal damage")
    void regenerationShieldPreventsLethalDamage() {
        Permanent alligator = addCreatureReady(player1, new RootwaterAlligator());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        alligator.setMarkedDamage(gqs.getEffectiveToughness(gd, alligator));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alligator);
        assertThat(alligator.getRegenerationShield()).isZero();
        assertThat(alligator.getMarkedDamage()).isZero();
        assertThat(alligator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without a Forest")
    void requiresForest() {
        harness.addToBattlefield(player1, new RootwaterAlligator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot sacrifice a non-Forest land")
    void requiresForestSubtype() {
        harness.addToBattlefield(player1, new RootwaterAlligator());
        harness.addToBattlefield(player1, new CityOfTraitors());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot sacrifice an opponent's Forest")
    void cannotSacrificeOpponentsForest() {
        harness.addToBattlefield(player1, new RootwaterAlligator());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Alligator can activate its regeneration ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent alligator = harness.addToBattlefieldAndReturn(player1, new RootwaterAlligator());
        alligator.setSummoningSick(true);
        alligator.tap();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(alligator.getRegenerationShield()).isEqualTo(1);
        assertThat(alligator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creating a regeneration shield does not immediately tap or heal the creature")
    void creatingShieldDoesNotRegenerateImmediately() {
        Permanent alligator = addCreatureReady(player1, new RootwaterAlligator());
        alligator.setMarkedDamage(1);
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(alligator.getRegenerationShield()).isEqualTo(1);
        assertThat(alligator.getMarkedDamage()).isEqualTo(1);
        assertThat(alligator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Repeated activations protect against separate lethal damage events")
    void repeatedActivationsCreateSeparateShields() {
        Permanent alligator = addCreatureReady(player1, new RootwaterAlligator());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, firstForest.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(alligator.getRegenerationShield()).isEqualTo(2);

        alligator.setMarkedDamage(gqs.getEffectiveToughness(gd, alligator));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alligator);
        assertThat(alligator.getRegenerationShield()).isEqualTo(1);
        assertThat(alligator.getMarkedDamage()).isZero();

        alligator.setMarkedDamage(gqs.getEffectiveToughness(gd, alligator));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(alligator);
        assertThat(alligator.getRegenerationShield()).isZero();
        assertThat(alligator.getMarkedDamage()).isZero();

        alligator.setMarkedDamage(gqs.getEffectiveToughness(gd, alligator));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Rootwater Alligator");
        harness.assertInGraveyard(player1, "Rootwater Alligator");
    }
}
