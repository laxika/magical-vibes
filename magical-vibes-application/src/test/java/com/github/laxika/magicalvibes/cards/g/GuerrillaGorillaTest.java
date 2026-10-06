package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuerrillaGorilla.class, GloriousAnthem.class, GrizzlyBears.class, LeoninScimitar.class,
        Ornithopter.class})
class GuerrillaGorillaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Guerrilla Gorilla and destroys a noncreature artifact")
    void destroysTargetArtifact() {
        addReadyGorilla(player1);
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Guerrilla Gorilla");
        harness.assertInGraveyard(player1, "Guerrilla Gorilla");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Destroys a noncreature enchantment")
    void destroysTargetEnchantment() {
        addReadyGorilla(player1);
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyGorilla(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addReadyGorilla(player1);
        Permanent artifactCreature = addCreatureReady(player2, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate outside sorcery speed")
    void cannotActivateOutsideSorcerySpeed() {
        addReadyGorilla(player1);
        Permanent target = addArtifact(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificesAsCostBeforeTargetIsDestroyed() {
        addReadyGorilla(player1);
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Guerrilla Gorilla");
        harness.assertNotOnBattlefield(player1, "Guerrilla Gorilla");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GuerrillaGorilla());
        gorilla.setSummoningSick(true);
        gorilla.tap();
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Guerrilla Gorilla");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void canDestroyOwnEnchantmentDuringPostcombatMainPhase() {
        addReadyGorilla(player1);
        Permanent target = addEnchantment(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Guerrilla Gorilla");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addReadyGorilla(player1);
        Permanent target = addArtifact(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Guerrilla Gorilla");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        addReadyGorilla(player1);
        addReadyGorilla(player1);
        Permanent target = addArtifact(player2);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Guerrilla Gorilla");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    private Permanent addReadyGorilla(Player player) {
        return addCreatureReady(player, new GuerrillaGorilla());
    }

    private Permanent addArtifact(Player player) {
        return addCreatureReady(player, new LeoninScimitar());
    }

    private Permanent addEnchantment(Player player) {
        return addCreatureReady(player, new GloriousAnthem());
    }
}
