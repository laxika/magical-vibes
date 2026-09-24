package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SphereOfPurity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AltarsLight.class, Ornithopter.class, SphereOfPurity.class, AuriokTransfixer.class})
class AltarsLightTest extends BaseCardTest {

    private void prepareAltarsLight() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AltarsLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Exiles target artifact")
    void exilesArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID targetId = harness.getPermanentId(player2, "Ornithopter");
        prepareAltarsLight();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Exiles target enchantment")
    void exilesEnchantment() {
        harness.addToBattlefield(player2, new SphereOfPurity());
        UUID targetId = harness.getPermanentId(player2, "Sphere of Purity");
        prepareAltarsLight();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Sphere of Purity");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Sphere of Purity"));
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AuriokTransfixer());
        UUID targetId = harness.getPermanentId(player2, "Auriok Transfixer");

        prepareAltarsLight();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
