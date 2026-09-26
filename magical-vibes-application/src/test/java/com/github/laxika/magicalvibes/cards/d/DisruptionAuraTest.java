package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GuardianIdol;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptionAura.class, GuardianIdol.class, DrossCrocodile.class})
class DisruptionAuraTest extends BaseCardTest {

    @Test
    @DisplayName("Disruption Aura can enchant an artifact")
    void canEnchantArtifact() {
        Permanent artifact = addArtifact(player2);
        harness.setHand(player1, List.of(new DisruptionAura()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached() && artifact.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Disruption Aura cannot enchant a non-artifact")
    void cannotEnchantNonArtifact() {
        Permanent artifact = addArtifact(player2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());

        harness.setHand(player1, List.of(new DisruptionAura()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices the enchanted artifact")
    void decliningPaymentSacrificesEnchantedArtifact() {
        Permanent artifact = addArtifact(player2);
        attachDisruptionAura(artifact);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Guardian Idol");
    }

    @Test
    @DisplayName("Paying the enchanted artifact's mana cost keeps it on the battlefield")
    void payingManaCostKeepsEnchantedArtifact() {
        Permanent artifact = addArtifact(player2);
        attachDisruptionAura(artifact);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Guardian Idol");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The enchanted artifact's controller gets the upkeep choice")
    void enchantedArtifactControllerGetsUpkeepChoice() {
        Permanent artifact = addArtifact(player2);
        attachDisruptionAura(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Guardian Idol");
    }

    private void attachDisruptionAura(Permanent artifact) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DisruptionAura());
        aura.setAttachedTo(artifact.getId());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GuardianIdol());
    }
}
