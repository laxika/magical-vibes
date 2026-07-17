package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarpArtifactTest extends BaseCardTest {

    // ===== Targeting =====

    @Test
    @DisplayName("Can enchant an artifact with Warp Artifact")
    void canEnchantArtifact() {
        Permanent artifact = addArtifact(player2);

        harness.setHand(player1, List.of(new WarpArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a non-artifact creature")
    void cannotEnchantCreature() {
        addArtifact(player2); // a legal target exists so the Aura is playable
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new WarpArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Resolving Warp Artifact attaches it to the target artifact")
    void resolvingAttachesToArtifact() {
        Permanent artifact = addArtifact(player2);

        harness.setHand(player1, List.of(new WarpArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Warp Artifact")
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    // ===== Upkeep damage =====

    @Test
    @DisplayName("Enchanted artifact's controller takes 1 damage at their upkeep")
    void enchantedControllerTakesDamageAtUpkeep() {
        Permanent artifact = addArtifact(player2);
        attachWarpArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Warp Artifact does NOT damage the aura controller during their own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent artifact = addArtifact(player2);
        attachWarpArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Damage accumulates over multiple upkeeps")
    void damageAccumulatesOverUpkeeps() {
        Permanent artifact = addArtifact(player2);
        attachWarpArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Helpers =====

    private void attachWarpArtifact(Permanent artifact) {
        Permanent warpArtifact = new Permanent(new WarpArtifact());
        warpArtifact.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(warpArtifact);
    }

    private Permanent addArtifact(Player player) {
        Card card = new Card();
        card.setName("Test Artifact");
        card.setType(CardType.ARTIFACT);
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
