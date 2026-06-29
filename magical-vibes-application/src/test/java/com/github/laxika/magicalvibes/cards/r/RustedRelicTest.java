package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RustedRelicTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has metalcraft static AnimateSelfWithStatsEffect with 5/5 Golem")
    void hasCorrectEffect() {
        RustedRelic card = new RustedRelic();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(AnimateSelfWithStatsEffect.class);

        AnimateSelfWithStatsEffect animate = (AnimateSelfWithStatsEffect) metalcraft.wrapped();
        assertThat(animate.power()).isEqualTo(5);
        assertThat(animate.toughness()).isEqualTo(5);
        assertThat(animate.grantedSubtypes()).containsExactly(CardSubtype.GOLEM);
    }

    // ===== Without metalcraft =====

    @Test
    @DisplayName("Not a creature with zero other artifacts")
    void notCreatureWithZeroArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).getFirst();
        // Rusted Relic itself is 1 artifact, need 3 total
        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    @Test
    @DisplayName("Not a creature with only two total artifacts")
    void notCreatureWithTwoArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rusted Relic"))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }

    // ===== With metalcraft =====

    @Test
    @DisplayName("Becomes a 5/5 creature with exactly three artifacts")
    void becomesCreatureWithThreeArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rusted Relic"))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    @Test
    @DisplayName("Has Golem subtype with metalcraft active")
    void hasGolemSubtypeWithMetalcraft() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rusted Relic"))
                .findFirst().orElseThrow();
        var bonus = gqs.computeStaticBonus(gd, relic);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOLEM);
    }

    @Test
    @DisplayName("Becomes a 5/5 creature with more than three artifacts")
    void becomesCreatureWithFourArtifacts() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rusted Relic"))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(5);
    }

    // ===== Metalcraft lost =====

    @Test
    @DisplayName("Stops being a creature when artifact count drops below three")
    void losesCreatureStatusWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rusted Relic"))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(5);

        // Remove one artifact — now only 2 total (Rusted Relic + Spellbook)
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gqs.isCreature(gd, relic)).isFalse();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(0);
    }

    // ===== Opponent artifacts don't count =====

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new RustedRelic());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        Permanent relic = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, relic)).isFalse();
    }
}
