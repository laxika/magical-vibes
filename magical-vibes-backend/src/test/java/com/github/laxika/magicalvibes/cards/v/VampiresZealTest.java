package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetIfSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VampiresZealTest extends BaseCardTest {

    // ===== Effect structure =====

    @Test
    @DisplayName("Vampire's Zeal has correct effects")
    void hasCorrectEffects() {
        VampiresZeal card = new VampiresZeal();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(BoostTargetCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GrantKeywordToTargetIfSubtypeEffect.class);

        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

    // ===== Non-vampire gets boost but NOT first strike =====

    @Test
    @DisplayName("Resolving on non-Vampire creature gives +2/+2 but NOT first strike")
    void nonVampireGetsBoostButNotFirstStrike() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Vampire gets boost AND first strike =====

    @Test
    @DisplayName("Resolving on Vampire creature gives +2/+2 AND first strike")
    void vampireGetsBoostAndFirstStrike() {
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Vampire Interloper");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Vampire Interloper");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent vampire = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vampire's Zeal"));
    }

    // ===== Can target opponent's creature =====

    @Test
    @DisplayName("Can target opponent's Vampire creature and grant first strike")
    void canTargetOpponentVampire() {
        harness.addToBattlefield(player2, new VampireInterloper());
        harness.setHand(player1, List.of(new VampiresZeal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Vampire Interloper");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent vampire = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(2);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }
}
