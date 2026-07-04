package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LumaretsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Lumaret's Favor has a boost effect and a self-cast copy trigger")
    void hasCorrectProperties() {
        LumaretsFavor card = new LumaretsFavor();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(4);

        assertThat(card.getEffects(EffectSlot.ON_SELF_CAST)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_SELF_CAST).getFirst())
                .isInstanceOf(CopyThisSpellIfConditionEffect.class);
    }

    @Test
    @DisplayName("Without gaining life, no copy is made and target gets +2/+4")
    void noLifeGainedNoCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        // Copy trigger always goes on the stack; the "if you gained life" clause fails at resolution.
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
        assertThat(gd.gameLog).noneMatch(log -> log.contains("A copy of Lumaret's Favor"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("After gaining life, the spell is copied and both instances resolve for +4/+8")
    void lifeGainedCopiesSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LumaretsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.getGameData().lifeGainedThisTurn.put(player1.getId(), 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);

        GameData gd = harness.getGameData();
        // Copy trigger sits above the spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve the copy trigger — it creates a copy and offers new targets.
        harness.passBothPriorities();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("A copy of Lumaret's Favor"));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Decline to choose new targets — the copy keeps the original target.
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the copy, then the original spell.
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }
}
