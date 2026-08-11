package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelicBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Can target an artifact")
    void canTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new RelicBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-artifact")
    void cannotTargetNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RelicBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enchanted artifact's controller loses 2 life at the beginning of their upkeep")
    void enchantedArtifactControllerLosesLifeAtUpkeep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castAndResolveRelicBane(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int auraControllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(auraControllerLifeBefore);
    }

    @Test
    @DisplayName("The trigger does not fire during the Aura controller's upkeep")
    void triggerDoesNotFireDuringAuraControllerUpkeep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castAndResolveRelicBane(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The trigger stops when Relic Bane is no longer attached")
    void triggerStopsWhenAuraLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castAndResolveRelicBane(artifact);
        Permanent aura = findPermanent(player1, "Relic Bane");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    private void castAndResolveRelicBane(Permanent artifact) {
        harness.setHand(player1, List.of(new RelicBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
    }
}
