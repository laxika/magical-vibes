package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

class IllusionaryServantTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Illusionary Servant has ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY SacrificeSelfEffect")
    void hasCorrectEffect() {
        IllusionaryServant card = new IllusionaryServant();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY).getFirst())
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    // ===== Sacrifice when targeted by opponent's spell =====

    @Test
    @DisplayName("Illusionary Servant is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent servantPerm = new Permanent(new IllusionaryServant());
        servantPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(servantPerm);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, servantPerm.getId());

        // Stack should have Shock + Illusionary Servant's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Illusionary Servant's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Illusionary Servant should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Illusionary Servant"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Illusionary Servant"));
    }

    // ===== Sacrifice when targeted by own controller's spell =====

    @Test
    @DisplayName("Illusionary Servant is sacrificed when targeted by its controller's spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent servantPerm = new Permanent(new IllusionaryServant());
        servantPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(servantPerm);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, servantPerm.getId());

        // Stack should have Giant Growth + Illusionary Servant's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Illusionary Servant's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Illusionary Servant should be sacrificed even when targeted by own controller
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Illusionary Servant"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Illusionary Servant"));
    }

    // ===== Sacrifice when targeted by an ability =====

    @Test
    @DisplayName("Illusionary Servant is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent servantPerm = new Permanent(new IllusionaryServant());
        servantPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(servantPerm);

        // Use Prodigal Pyromancer to target Illusionary Servant with an activated ability
        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyroPerm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prodigal Pyromancer"))
                .findFirst().orElseThrow();
        pyroPerm.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyroPerm),
                null, servantPerm.getId());

        // Stack should have Prodigal Pyromancer's ability + Illusionary Servant's sacrifice trigger on top
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        // Resolve Illusionary Servant's triggered ability first (it's on top)
        harness.passBothPriorities();

        // Illusionary Servant should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Illusionary Servant"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Illusionary Servant"));
    }
}
