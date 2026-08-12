package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OxidizeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");

        castOxidize(targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Target artifact cannot be regenerated")
    void cannotBeRegenerated() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(targetId))
                .findFirst()
                .orElseThrow();
        target.setRegenerationShield(1);

        castOxidize(targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOxidize(UUID targetId) {
        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
