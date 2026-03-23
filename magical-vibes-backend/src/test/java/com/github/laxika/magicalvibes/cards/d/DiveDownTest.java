package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiveDownTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Dive Down has correct card properties")
    void hasCorrectProperties() {
        DiveDown card = new DiveDown();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(boost.powerBoost()).isEqualTo(0);
        assertThat(boost.toughnessBoost()).isEqualTo(3);

        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(grant.keywords()).containsExactly(Keyword.HEXPROOF);
        assertThat(grant.scope()).isEqualTo(GrantScope.TARGET);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Dive Down puts it on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiveDown()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dive Down");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Dive Down gives +0/+3 and hexproof to target creature")
    void resolvingBoostsAndGrantsHexproof() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiveDown()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost and hexproof wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiveDown()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Dive Down fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiveDown()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dive Down"));
    }
}
