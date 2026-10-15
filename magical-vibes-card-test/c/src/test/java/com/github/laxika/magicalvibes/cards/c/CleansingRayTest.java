package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ExpelFromOrazca;
import com.github.laxika.magicalvibes.cards.r.RadiantDestiny;
import com.github.laxika.magicalvibes.cards.s.SanguineGlorifier;
import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleansingRay.class, SanguineGlorifier.class, RadiantDestiny.class, SunSentinel.class, ExpelFromOrazca.class})
class CleansingRayTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target Vampire")
    @CardUsed({CleansingRay.class, SanguineGlorifier.class, RadiantDestiny.class, SunSentinel.class})
    class VampireMode {

        @Test
        void destroysVampire() {
            harness.addToBattlefield(player2, new SanguineGlorifier());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 0, harness.getPermanentId(player2, "Sanguine Glorifier"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Sanguine Glorifier");
            harness.assertInGraveyard(player2, "Sanguine Glorifier");
        }

        @Test
        @DisplayName("Cannot target an enchantment with the Vampire mode")
        void cannotTargetEnchantment() {
            harness.addToBattlefieldAndReturn(player2, new RadiantDestiny()).setChosenSubtype(CardSubtype.HUMAN);
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(
                    player1, 0, 0, harness.getPermanentId(player2, "Radiant Destiny")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotTargetNonVampireCreature() {
            harness.addToBattlefield(player2, new SunSentinel());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(
                    player1, 0, 0, harness.getPermanentId(player2, "Sun Sentinel")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void canDestroyOwnVampire() {
            harness.addToBattlefield(player1, new SanguineGlorifier());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Sanguine Glorifier"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Sanguine Glorifier");
            harness.assertInGraveyard(player1, "Sanguine Glorifier");
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target enchantment")
    @CardUsed({CleansingRay.class, RadiantDestiny.class, SunSentinel.class, SanguineGlorifier.class})
    class EnchantmentMode {

        @Test
        void destroysEnchantment() {
            harness.addToBattlefieldAndReturn(player2, new RadiantDestiny()).setChosenSubtype(CardSubtype.HUMAN);
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 1, harness.getPermanentId(player2, "Radiant Destiny"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Radiant Destiny");
            harness.assertInGraveyard(player2, "Radiant Destiny");
        }

        @Test
        @DisplayName("Cannot target a non-Vampire creature with the enchantment mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new SunSentinel());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(
                    player1, 0, 1, harness.getPermanentId(player2, "Sun Sentinel")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotTargetVampireWithEnchantmentMode() {
            harness.addToBattlefield(player2, new SanguineGlorifier());
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            assertThatThrownBy(() -> harness.castSorcery(
                    player1, 0, 1, harness.getPermanentId(player2, "Sanguine Glorifier")))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void canDestroyOwnEnchantment() {
            harness.addToBattlefieldAndReturn(player1, new RadiantDestiny()).setChosenSubtype(CardSubtype.HUMAN);
            harness.setHand(player1, List.of(new CleansingRay()));
            harness.addMana(player1, ManaColor.WHITE, 2);

            harness.castSorcery(player1, 0, 1, harness.getPermanentId(player1, "Radiant Destiny"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Radiant Destiny");
            harness.assertInGraveyard(player1, "Radiant Destiny");
        }
    }

    @Test
    @CardUsed({CleansingRay.class, SanguineGlorifier.class, ExpelFromOrazca.class})
    void doesNotDestroyVampireReturnedToHandInResponse() {
        harness.addToBattlefield(player2, new SanguineGlorifier());
        harness.setHand(player1, List.of(new CleansingRay()));
        harness.setHand(player2, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        var targetId = harness.getPermanentId(player2, "Sanguine Glorifier");

        harness.castSorcery(player1, 0, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Sanguine Glorifier");
        harness.assertNotInGraveyard(player2, "Sanguine Glorifier");
        harness.assertInGraveyard(player1, "Cleansing Ray");
    }
}
