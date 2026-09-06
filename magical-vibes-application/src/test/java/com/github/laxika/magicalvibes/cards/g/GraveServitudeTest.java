package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.v.VernalEquinox;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharcoalDiamond.class, DarkRitual.class, FemerefScouts.class, GraveServitude.class})
class GraveServitudeTest extends BaseCardTest {

    private Permanent enchant(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GraveServitude());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +3/-1 and is black instead of its own colors")
    void enchantedCreatureGetsBoostAndBecomesBlack() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        enchant(scouts);

        assertThat(gqs.getEffectivePower(gd, scouts)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, scouts, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasColor(gd, scouts, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Boost and color change are lost when Grave Servitude leaves the battlefield")
    void effectsLostWhenRemoved() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        Permanent aura = enchant(scouts);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, scouts)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(4);
        assertThat(gqs.hasColor(gd, scouts, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasColor(gd, scouts, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Grave Servitude");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grave Servitude");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Grave Servitude");
        harness.assertInGraveyard(player1, "Grave Servitude");
    }

    @Test
    @DisplayName("Cast during a main phase with a spell on the stack, it is sacrificed at cleanup")
    void castWithNonEmptyStackIsSacrificedAtCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grave Servitude");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Grave Servitude");
        harness.assertInGraveyard(player1, "Grave Servitude");
    }

    @Test
    @CardUsed(VernalEquinox.class)
    @DisplayName("Cast using another flash permission, it survives cleanup")
    void castUsingAnotherFlashPermissionSurvivesCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.addToBattlefield(player1, new VernalEquinox());
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grave Servitude");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Grave Servitude");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new FemerefScouts());
        harness.addToBattlefield(player1, new CharcoalDiamond());
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = findPermanent(player1, "Charcoal Diamond");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
