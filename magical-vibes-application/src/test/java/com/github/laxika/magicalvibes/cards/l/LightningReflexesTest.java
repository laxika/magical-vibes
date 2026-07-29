package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LightningReflexesTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and first strike")
    void enchantedCreatureGetsBoostAndFirstStrike() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and first strike are lost when the Aura leaves the battlefield")
    void boostLostWhenAuraRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Lightning Reflexes");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Lightning Reflexes");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightning Reflexes");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Lightning Reflexes");
        harness.assertInGraveyard(player1, "Lightning Reflexes");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
