package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
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

@CardUsed({ArmorOfThorns.class, BayFalcon.class, FeralShadow.class, CharcoalDiamond.class})
class ArmorOfThornsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());

        Permanent aura = new Permanent(new ArmorOfThorns());
        aura.setAttachedTo(falcon.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, falcon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, falcon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.setHand(player1, List.of(new ArmorOfThorns()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, falcon)).isEqualTo(3);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Armor of Thorns");
    }

    @Test
    @DisplayName("Cast during a main phase with a spell on the stack, it is sacrificed at cleanup")
    void castWithAnotherSpellOnStackIsSacrificedAtCleanup() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.castFromHand(player1, new BayFalcon(), "{1}{U}");
        harness.setHand(player1, List.of(new ArmorOfThorns()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Armor of Thorns");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Armor of Thorns");
        harness.assertInGraveyard(player1, "Armor of Thorns");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.setHand(player1, List.of(new ArmorOfThorns()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, falcon.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Armor of Thorns");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Armor of Thorns");
        harness.assertInGraveyard(player1, "Armor of Thorns");
    }

    @Test
    @DisplayName("Cannot enchant a black creature")
    void cannotEnchantBlackCreature() {
        Permanent shadow = addCreatureReady(player2, new FeralShadow());
        harness.setHand(player1, List.of(new ArmorOfThorns()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, shadow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CharcoalDiamond());
        harness.setHand(player1, List.of(new ArmorOfThorns()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }
}
