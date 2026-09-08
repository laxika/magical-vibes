package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CleansingRayTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target Vampire")
    class VampireMode {

        @Test
        void destroysVampire() {
            harness.addToBattlefield(player2, new VampireAristocrat());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 0, harness.getPermanentId(player2, "Vampire Aristocrat"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Vampire Aristocrat");
            harness.assertInGraveyard(player2, "Vampire Aristocrat");
        }

        @Test
        @DisplayName("Cannot target an enchantment with the Vampire mode")
        void cannotTargetEnchantment() {
            harness.addToBattlefield(player2, new GhostlyPrison());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(
                    player1, 0, 0, harness.getPermanentId(player2, "Ghostly Prison")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target enchantment")
    class EnchantmentMode {

        @Test
        void destroysEnchantment() {
            harness.addToBattlefield(player2, new GhostlyPrison());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 1, harness.getPermanentId(player2, "Ghostly Prison"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Ghostly Prison");
            harness.assertInGraveyard(player2, "Ghostly Prison");
        }

        @Test
        @DisplayName("Cannot target a non-Vampire creature with the enchantment mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(
                    player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears")))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
