package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AcidicDagger;
import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardOfLights.class, BayFalcon.class, AcidicDagger.class})
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
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        attachWard(falcon, CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature gains no protection from other colors")
    void noProtectionFromOtherColors() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        attachWard(falcon, CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Without a chosen color the enchanted creature has no protection")
    void noProtectionBeforeColorIsChosen() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        attachWard(falcon, null);

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, falcon, color)).isFalse();
        }
    }

    @Test
    @DisplayName("Protection is lost when Ward of Lights leaves the battlefield")
    void protectionLostWhenRemoved() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        Permanent aura = attachWard(falcon, CardColor.RED);

        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.RED)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Choosing white does not remove the Aura itself")
    void choosingWhiteDoesNotRemoveTheAura() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        attachWard(falcon, CardColor.WHITE);

        boolean changed = harness.getPermanentRemovalService().enforceAttachmentLegality(gd);

        assertThat(changed).isFalse();
        harness.assertOnBattlefield(player1, "Ward of Lights");
        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.BLACK)).isTrue();

        harness.passUntil(TurnStep.CLEANUP);

        harness.assertOnBattlefield(player1, "Ward of Lights");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.assertOnBattlefield(player1, "Ward of Lights");

        harness.passUntil(TurnStep.CLEANUP);

        harness.assertOnBattlefield(player1, "Ward of Lights");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ward of Lights");
        harness.assertInGraveyard(player1, "Ward of Lights");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new AcidicDagger());
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = findPermanent(player1, "Acidic Dagger");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Choosing white while resolving keeps the Aura attached and protects the creature")
    void choosingWhiteDuringResolutionKeepsAuraAttached() {
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        Permanent aura = findPermanent(player1, "Ward of Lights");
        assertThat(aura.getAttachedTo()).isEqualTo(falcon.getId());
        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.WHITE)).isTrue();
        assertThat(harness.getPermanentRemovalService().enforceAttachmentLegality(gd)).isFalse();
        harness.assertOnBattlefield(player1, "Ward of Lights");

        harness.passUntil(TurnStep.CLEANUP);

        harness.assertOnBattlefield(player1, "Ward of Lights");
        harness.assertOnBattlefield(player2, "Bay Falcon");
    }

    @Test
    @DisplayName("Casting in response during your main phase still requires the cleanup sacrifice")
    void castingWithNonemptyStackRequiresCleanupSacrifice() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.setHand(player1, List.of(new BayFalcon(), new WardOfLights()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.passBothPriorities();

        harness.passUntil(TurnStep.CLEANUP);
        harness.assertOnBattlefield(player1, "Ward of Lights");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ward of Lights");
        harness.assertInGraveyard(player1, "Ward of Lights");
        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.BLACK)).isFalse();
        assertThat(countPermanents(player1, "Bay Falcon")).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting during the opponent's main phase requires the cleanup sacrifice")
    void castingDuringOpponentsMainPhaseRequiresCleanupSacrifice() {
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WardOfLights()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.RED)).isTrue();
        harness.passUntil(TurnStep.CLEANUP);
        harness.assertOnBattlefield(player1, "Ward of Lights");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ward of Lights");
        harness.assertInGraveyard(player1, "Ward of Lights");
        harness.assertOnBattlefield(player2, "Bay Falcon");
        assertThat(gqs.hasProtectionFrom(gd, falcon, CardColor.RED)).isFalse();
    }
}
