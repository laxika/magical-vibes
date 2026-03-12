package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhantomBeastTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Phantom Beast has ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY SacrificeSelfEffect")
    void hasCorrectEffect() {
        PhantomBeast card = new PhantomBeast();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY).getFirst())
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    // ===== Sacrifice when targeted by opponent's spell =====

    @Test
    @DisplayName("Phantom Beast is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent phantomPerm = new Permanent(new PhantomBeast());
        phantomPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(phantomPerm);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, phantomPerm.getId());

        // Stack should have Shock + Phantom Beast's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Phantom Beast's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Phantom Beast should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phantom Beast"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phantom Beast"));
    }

    // ===== Sacrifice when targeted by own controller's spell =====

    @Test
    @DisplayName("Phantom Beast is sacrificed when targeted by its controller's spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent phantomPerm = new Permanent(new PhantomBeast());
        phantomPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(phantomPerm);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, phantomPerm.getId());

        // Stack should have Giant Growth + Phantom Beast's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Phantom Beast's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Phantom Beast should be sacrificed even when targeted by own controller
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phantom Beast"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phantom Beast"));
    }

    // ===== Sacrifice when targeted by an ability =====

    @Test
    @DisplayName("Phantom Beast is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent phantomPerm = new Permanent(new PhantomBeast());
        phantomPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(phantomPerm);

        // Use Prodigal Pyromancer to target Phantom Beast with an activated ability
        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyroPerm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prodigal Pyromancer"))
                .findFirst().orElseThrow();
        pyroPerm.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyroPerm),
                null, phantomPerm.getId());

        // Stack should have Prodigal Pyromancer's ability + Phantom Beast's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Phantom Beast's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Phantom Beast should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phantom Beast"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phantom Beast"));
    }
}
