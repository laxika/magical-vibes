package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReturnToNatureTest extends BaseCardTest {

    private void setUpSpell() {
        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new FountainOfYouth());
            setUpSpell();

            UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
            harness.castInstant(player1, 0, 0, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        }

        @Test
        @DisplayName("Cannot target a non-artifact")
        void cannotTargetNonArtifact() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player1, new FountainOfYouth());
            setUpSpell();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target enchantment")
    class DestroyEnchantmentMode {

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GloriousAnthem());
            setUpSpell();

            UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        }
    }

    @Test
    @DisplayName("Mode 2: Exiles target card from a graveyard")
    void exilesGraveyardCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        setUpSpell();

        harness.castInstant(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
