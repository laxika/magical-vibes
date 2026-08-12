package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelicCrushTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact and an enchantment")
    void destroysArtifactAndEnchantment() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys the mandatory target when the other target is omitted")
    void destroysOneTarget() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, List.of(artifactId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Cannot target a creature as the other target")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifactId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The other target must be different from the first target")
    void cannotTargetSamePermanentTwice() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifactId, artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
