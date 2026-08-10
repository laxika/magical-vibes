package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DizzyingGazeTest extends BaseCardTest {

    private Permanent addAuraToCreature(Permanent creature) {
        gd.playerBattlefields.get(player1.getId()).add(creature);
        Permanent aura = new Permanent(new DizzyingGaze());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    void enchantedCreatureDealsDamageToFlyingCreature() {
        addAuraToCreature(new Permanent(new TyphoidRats()));
        Permanent target = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void cannotTargetCreatureWithoutFlying() {
        addAuraToCreature(new Permanent(new GrizzlyBears()));
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    void canEnchantOnlyCreatureYouControl() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new GrizzlyBears()));
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);
        harness.setHand(player1, List.of(new DizzyingGaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
