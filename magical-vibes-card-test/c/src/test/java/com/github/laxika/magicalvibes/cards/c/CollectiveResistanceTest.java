package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({CollectiveResistance.class, ArcaneSignet.class, CampaignOfVengeance.class, GrizzlyBears.class})
class CollectiveResistanceTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode destroys an artifact")
    void destroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        cast(new int[]{0}, List.of(artifact.getId()), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Arcane Signet");
        harness.assertInGraveyard(player2, "Arcane Signet");
    }

    @Test
    @DisplayName("Enchantment mode destroys an enchantment")
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CampaignOfVengeance());
        cast(new int[]{1}, List.of(enchantment.getId()), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Campaign of Vengeance");
        harness.assertInGraveyard(player2, "Campaign of Vengeance");
    }

    @Test
    @DisplayName("Creature mode grants hexproof and indestructible until end of turn")
    void grantsHexproofAndIndestructibleUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        cast(new int[]{2}, List.of(creature.getId()), 1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("All modes resolve and pay one green for each additional mode")
    void allModesResolveWithEscalate() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ArcaneSignet());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CampaignOfVengeance());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveResistance()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(artifact.getId(), enchantment.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arcane Signet");
        harness.assertInGraveyard(player2, "Campaign of Vengeance");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Artifact mode rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveResistance()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void cast(int[] modes, List<UUID> targets, int greenMana) {
        harness.setHand(player1, List.of(new CollectiveResistance()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.castModalInstantWithModes(player1, 0, 1, 3, modes, targets);
    }
}
