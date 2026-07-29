package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WardOfLightsTest extends BaseCardTest {

    private Permanent attachWard(Permanent host, CardColor chosenColor) {
        Permanent aura = new Permanent(new WardOfLights());
        aura.setAttachedTo(host.getId());
        aura.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature has protection from the chosen color")
    void enchantedCreatureHasProtectionFromChosenColor() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachWard(bears, CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature gains no protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachWard(bears, CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Without a chosen color the enchanted creature has no protection")
    void noProtectionBeforeColorIsChosen() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachWard(bears, null);

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, bears, color)).isFalse();
        }
    }

    @Test
    @DisplayName("Protection is lost when Ward of Lights leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachWard(bears, CardColor.RED);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Choosing white does not remove the Aura itself")
    void choosingWhiteDoesNotRemoveTheAura() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachWard(bears, CardColor.WHITE);

        boolean changed = GameTestEngineContext.get().getBean(PermanentRemovalService.class)
                .enforceAttachmentLegality(gd);

        assertThat(changed).isFalse();
        harness.assertOnBattlefield(player1, "Ward of Lights");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Ward of Lights");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.assertOnBattlefield(player1, "Ward of Lights");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Ward of Lights");
        harness.assertInGraveyard(player1, "Ward of Lights");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
