package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsignToDustTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys any number of target artifacts and enchantments")
    void destroysMixedTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());

        castConsignToDust(List.of(artifact.getId(), aura.getId()), 2, 4);

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Strive requires {2}{G} for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target no permanents")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can target only artifacts and enchantments")
    void targetsMustBeArtifactsOrEnchantments() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID creatureId = creature.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Targets must be artifacts and/or enchantments");
    }

    private void castConsignToDust(List<UUID> targetIds, int greenMana, int colorlessMana) {
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
