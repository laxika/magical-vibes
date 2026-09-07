package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarpArtifact.class, GrizzlyBears.class, Ornithopter.class, StealArtifact.class, Disenchant.class})
class WarpArtifactTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant an artifact with Warp Artifact")
    void canEnchantArtifact() {
        Permanent artifact = addArtifact(player2);

        harness.setHand(player1, List.of(new WarpArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a non-artifact creature")
    void cannotEnchantCreature() {
        addArtifact(player2); // a legal target exists so the Aura is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WarpArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Resolving Warp Artifact attaches it to the target artifact")
    void resolvingAttachesToArtifact() {
        Permanent artifact = addArtifact(player2);
        WarpArtifact warpArtifact = new WarpArtifact();

        harness.setHand(player1, List.of(warpArtifact));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(warpArtifact.getId())
                        && p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

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

    @Test
    @DisplayName("Trigger damages the controller at trigger time if the artifact changes control before resolution")
    void triggerUsesControllerAtTriggerTime() {
        Permanent artifact = addArtifact(player1);
        attachWarpArtifact(artifact);

        StealArtifact stealArtifact = new StealArtifact();
        harness.setHand(player2, List.of(stealArtifact));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player2, 0, artifact.getId());
        harness.passBothPriorities();

        Permanent stealArtifactPermanent = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getId().equals(stealArtifact.getId()))
                .findFirst()
                .orElseThrow();

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);

        Disenchant disenchant = new Disenchant();
        harness.setHand(player1, List.of(disenchant));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, stealArtifactPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore - 1);
    }

    private void attachWarpArtifact(Permanent artifact) {
        Permanent warpArtifact = harness.addToBattlefieldAndReturn(player1, new WarpArtifact());
        warpArtifact.setAttachedTo(artifact.getId());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Ornithopter());
    }
}
