package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestorationSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one artifact and up to one enchantment from the graveyard")
    void returnsArtifactAndEnchantment() {
        Permanent specialist = addSpecialist();
        Card artifact = new Ornithopter();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(artifact, enchantment));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertInHand(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Restoration Specialist");
    }

    @Test
    @DisplayName("May sacrifice itself without choosing targets")
    void allowsNoTargets() {
        Permanent specialist = addSpecialist();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbilityWithGraveyardTargets(player1, specialistIndex(specialist), 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Restoration Specialist");
    }

    @Test
    @DisplayName("Rejects two artifact targets")
    void rejectsTwoArtifactTargets() {
        Permanent specialist = addSpecialist();
        Card firstArtifact = new Ornithopter();
        Card secondArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(firstArtifact, secondArtifact));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0,
                List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one artifact");
    }

    @Test
    @DisplayName("Rejects a creature target")
    void rejectsCreatureTarget() {
        Permanent specialist = addSpecialist();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, specialistIndex(specialist), 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSpecialist() {
        return harness.addToBattlefieldAndReturn(player1, new RestorationSpecialist());
    }

    private int specialistIndex(Permanent specialist) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(specialist);
    }
}
