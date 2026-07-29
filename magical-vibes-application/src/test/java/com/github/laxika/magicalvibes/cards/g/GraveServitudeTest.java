package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CardColor;
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

class GraveServitudeTest extends BaseCardTest {

    private Permanent enchant(Permanent host) {
        Permanent aura = new Permanent(new GraveServitude());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +3/-1 and is black instead of its own colors")
    void enchantedCreatureGetsBoostAndBecomesBlack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.hasColor(gd, bears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasColor(gd, bears, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Boost and color change are lost when Grave Servitude leaves the battlefield")
    void effectsLostWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = enchant(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasColor(gd, bears, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasColor(gd, bears, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Grave Servitude");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grave Servitude");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Grave Servitude");
        harness.assertInGraveyard(player1, "Grave Servitude");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
