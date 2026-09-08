package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Paying the enchanted artifact's mana cost keeps it on the battlefield")
    void payingManaCostKeepsEnchantedArtifact() {
        Permanent artifact = addArtifact(player2);
        attachDisruptionAura(artifact);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent attachDisruptionAura(Permanent artifact) {
        Permanent aura = new Permanent(new DisruptionAura());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private Permanent addArtifact(Player player) {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player.getId()).add(artifact);
        return artifact;
    }
}
