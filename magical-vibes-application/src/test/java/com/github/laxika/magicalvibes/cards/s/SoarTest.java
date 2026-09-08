package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
import com.github.laxika.magicalvibes.cards.v.VernalEquinox;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Soar.class, FemerefScouts.class, ManaPrism.class, DarkRitual.class})
class SoarTest extends BaseCardTest {

    private Permanent enchant(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Soar());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +0/+1 and has flying")
    void enchantedCreatureGetsBoostAndFlying() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        enchant(scouts);

        assertThat(gqs.getEffectivePower(gd, scouts)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, scouts, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying are lost when Soar leaves the battlefield")
    void effectsLostWhenRemoved() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        Permanent aura = enchant(scouts);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, scouts, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent scouts = addCreatureReady(player2, new FemerefScouts());
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, scouts)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, scouts, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Soar");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soar");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Soar");
        harness.assertInGraveyard(player1, "Soar");
    }

    @Test
    @DisplayName("Cast during a main phase with a spell on the stack, it is sacrificed at cleanup")
    void castWithNonEmptyStackIsSacrificedAtCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soar");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Soar");
        harness.assertInGraveyard(player1, "Soar");
    }

    @Test
    @CardUsed(VernalEquinox.class)
    @DisplayName("Casting with another flash permission does not cause the cleanup sacrifice")
    void castUsingAnotherFlashPermissionSurvivesCleanup() {
        Permanent scouts = addCreatureReady(player1, new FemerefScouts());
        harness.addToBattlefieldAndReturn(player1, new VernalEquinox());
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, scouts.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soar");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Soar");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new FemerefScouts());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ManaPrism());
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
