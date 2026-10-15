package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderhandedDesigns.class, Ornithopter.class, GrizzlyBears.class, MycosynthLattice.class})
class UnderhandedDesignsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} drains each opponent when an artifact you control enters")
    void payingOneManaDrainsEachOpponent() {
        addReadyDesigns(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining the artifact-entry payment has no effect")
    void decliningPaymentHasNoEffect() {
        addReadyDesigns(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sacrificing Underhanded Designs destroys a target creature with two artifacts")
    void sacrificesAndDestroysTargetCreature() {
        addReadyDesigns(player1);
        addArtifactReady(player1);
        addArtifactReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Underhanded Designs");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without two artifacts")
    void cannotActivateWithoutTwoArtifacts() {
        addReadyDesigns(player1);
        addArtifactReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({UnderhandedDesigns.class, MycosynthLattice.class})
    @DisplayName("Underhanded Designs triggers for its own entry when it is an artifact")
    void artifactDesignsTriggersForItsOwnEntry() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.enterBattlefieldAndReturn(player1, new UnderhandedDesigns());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An artifact-entry trigger still resolves after Designs is sacrificed")
    void triggerSurvivesSacrificingDesigns() {
        addReadyDesigns(player1);
        addArtifactReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Underhanded Designs");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Accepting without mana does not drain life")
    void cannotDrainWithoutPaying() {
        addReadyDesigns(player1);
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger Underhanded Designs")
    void opponentsArtifactDoesNotTrigger() {
        addReadyDesigns(player1);
        harness.enterBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A nonartifact creature does not trigger Underhanded Designs")
    void nonartifactDoesNotTrigger() {
        addReadyDesigns(player1);
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Each artifact entry offers a separate payment")
    void eachArtifactOffersSeparatePayment() {
        addReadyDesigns(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent artifacts do not satisfy the activation restriction")
    void opponentsArtifactsDoNotEnableActivation() {
        addReadyDesigns(player1);
        addArtifactReady(player1);
        addArtifactReady(player2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Underhanded Designs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The artifact-count restriction is checked only when activating")
    void losingArtifactsAfterActivationDoesNotPreventDestruction() {
        addReadyDesigns(player1);
        Permanent artifact = addArtifactReady(player1);
        addArtifactReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Underhanded Designs");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature")
    void cannotTargetNoncreature() {
        addReadyDesigns(player1);
        addArtifactReady(player1);
        addArtifactReady(player1);
        Permanent target = addReadyDesigns(player2);
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Underhanded Designs");
        harness.assertOnBattlefield(player2, "Underhanded Designs");
    }

    private Permanent addReadyDesigns(Player player) {
        return addCreatureReady(player, new UnderhandedDesigns());
    }

    private Permanent addArtifactReady(Player player) {
        return addCreatureReady(player, new Ornithopter());
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
