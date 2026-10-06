package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicBane.class, Ornithopter.class, Swamp.class})
class RelicBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Can target an artifact")
    void canTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new RelicBane()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-artifact")
    void cannotTargetNonArtifact() {
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new RelicBane()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
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

    @Test
    @DisplayName("Enchanting your own artifact makes you lose life")
    void ownArtifactControllerLosesLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castAndResolveRelicBane(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Two Relic Banes grant two separate upkeep abilities")
    void multipleAurasEachCauseLifeLoss() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castAndResolveRelicBane(artifact);
        castAndResolveRelicBane(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Removing the Aura after triggering does not stop life loss")
    void pendingTriggerSurvivesAuraLeaving() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castAndResolveRelicBane(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        Permanent aura = findPermanent(player1, "Relic Bane");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Removing the artifact after triggering does not stop life loss")
    void pendingTriggerSurvivesArtifactLeaving() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castAndResolveRelicBane(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(countPermanents(player1, "Relic Bane")).isZero();
    }
    private void castAndResolveRelicBane(Permanent artifact) {
        harness.setHand(player1, List.of(new RelicBane()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
    }
}
