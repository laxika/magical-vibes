package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChompingChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a target artifact")
    void etbDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castChompingChangeling(List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Chomping Changeling");
    }

    @Test
    @DisplayName("ETB destroys a target enchantment")
    void etbDestroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());

        castChompingChangeling(List.of(enchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Rule of Law");
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void etbCanResolveWithoutTarget() {
        castChompingChangeling(List.of());

        harness.assertOnBattlefield(player1, "Chomping Changeling");
    }

    @Test
    @DisplayName("ETB cannot target a non-artifact, non-enchantment permanent")
    void rejectsInvalidTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castChompingChangeling(List<UUID> targetIds) {
        prepareToCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new ChompingChangeling()));
        harness.addMana(player1, ManaColor.GREEN, 3);
    }
}
