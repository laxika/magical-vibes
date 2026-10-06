package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AdventuringGear;
import com.github.laxika.magicalvibes.cards.c.CliffThreader;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KhalniHeartExpedition;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicCrush.class, AdventuringGear.class, KhalniHeartExpedition.class, CliffThreader.class, IntoTheRoil.class})
class RelicCrushTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact and an enchantment")
    void destroysArtifactAndEnchantment() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new AdventuringGear()).getId();
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, new KhalniHeartExpedition()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, List.of(artifactId, enchantmentId));

        harness.assertInGraveyard(player2, "Adventuring Gear");
        harness.assertInGraveyard(player2, "Khalni Heart Expedition");
    }

    @Test
    @DisplayName("Destroys the mandatory target when the other target is omitted")
    void destroysOneTarget() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new AdventuringGear()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, List.of(artifactId));

        harness.assertInGraveyard(player2, "Adventuring Gear");
    }

    @Test
    @DisplayName("Cannot target a creature as the other target")
    void cannotTargetCreature() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new AdventuringGear()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new CliffThreader()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifactId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The other target must be different from the first target")
    void cannotTargetSamePermanentTwice() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new AdventuringGear()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifactId, artifactId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysTwoArtifactsWithDifferentControllers() {
        UUID first = harness.addToBattlefieldAndReturn(player1, new AdventuringGear()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player2, new AdventuringGear()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, List.of(first, second));

        harness.assertNotOnBattlefield(player1, "Adventuring Gear");
        harness.assertNotOnBattlefield(player2, "Adventuring Gear");
        harness.assertInGraveyard(player1, "Adventuring Gear");
        harness.assertInGraveyard(player2, "Adventuring Gear");
    }

    @Test
    void destroysTwoEnchantments() {
        UUID first = harness.addToBattlefieldAndReturn(player1, new KhalniHeartExpedition()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player2, new KhalniHeartExpedition()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, List.of(first, second));

        harness.assertInGraveyard(player1, "Khalni Heart Expedition");
        harness.assertInGraveyard(player2, "Khalni Heart Expedition");
    }

    @Test
    void destroysOneEnchantment() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new KhalniHeartExpedition()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, List.of(target));

        harness.assertInGraveyard(player2, "Khalni Heart Expedition");
    }

    @Test
    void cannotCastWithoutMandatoryTarget() {
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.<UUID>of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureAsMandatoryTarget() {
        UUID creature = harness.addToBattlefieldAndReturn(player2, new CliffThreader()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysSecondTargetWhenFirstLeavesBattlefield() {
        assertRemainingTargetDestroyed(true);
    }

    @Test
    void destroysFirstTargetWhenSecondLeavesBattlefield() {
        assertRemainingTargetDestroyed(false);
    }

    private void assertRemainingTargetDestroyed(boolean returnFirstTarget) {
        UUID artifact = harness.addToBattlefieldAndReturn(player2, new AdventuringGear()).getId();
        UUID enchantment = harness.addToBattlefieldAndReturn(player2, new KhalniHeartExpedition()).getId();
        harness.setHand(player1, List.of(new RelicCrush()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(artifact, enchantment));
        harness.castAndResolveInstant(player2, 0, returnFirstTarget ? artifact : enchantment);
        harness.passBothPriorities();

        String returned = returnFirstTarget ? "Adventuring Gear" : "Khalni Heart Expedition";
        String destroyed = returnFirstTarget ? "Khalni Heart Expedition" : "Adventuring Gear";
        harness.assertInHand(player2, returned);
        harness.assertNotInGraveyard(player2, returned);
        harness.assertInGraveyard(player2, destroyed);
        harness.assertInGraveyard(player1, "Relic Crush");
    }
}
