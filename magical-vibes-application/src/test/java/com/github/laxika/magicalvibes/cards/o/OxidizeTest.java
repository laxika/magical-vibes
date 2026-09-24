package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelPendant;
import com.github.laxika.magicalvibes.cards.s.SerumPowder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Oxidize.class, SerumPowder.class, DarksteelPendant.class, CrazedGoblin.class})
class OxidizeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerumPowder());

        castOxidize(target.getId());

        harness.assertNotOnBattlefield(player2, "Serum Powder");
        harness.assertInGraveyard(player2, "Serum Powder");
    }

    @Test
    @DisplayName("Target artifact cannot be regenerated")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerumPowder());
        target.setRegenerationShield(1);

        castOxidize(target.getId());

        harness.assertNotOnBattlefield(player2, "Serum Powder");
        harness.assertInGraveyard(player2, "Serum Powder");
    }

    @Test
    @DisplayName("Cannot destroy an indestructible artifact")
    void cannotDestroyIndestructibleArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelPendant());

        castOxidize(target.getId());

        harness.assertOnBattlefield(player2, "Darksteel Pendant");
        harness.assertNotInGraveyard(player2, "Darksteel Pendant");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin());
        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOxidize(UUID targetId) {
        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
