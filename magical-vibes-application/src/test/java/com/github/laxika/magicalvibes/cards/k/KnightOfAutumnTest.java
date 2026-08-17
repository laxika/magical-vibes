package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SealOfStrength;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnightOfAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode puts two +1/+1 counters on Knight of Autumn")
    void countersMode() {
        castKnight(0);
        resolveCreatureAndEtb();

        Permanent knight = findPermanent(player1, "Knight of Autumn");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB mode destroys target artifact")
    void destroysArtifactMode() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent artifact = gd.playerBattlefields.get(player2.getId()).getLast();

        castKnight(1, artifact.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("ETB mode destroys target enchantment")
    void destroysEnchantmentMode() {
        harness.addToBattlefield(player2, new SealOfStrength());
        Permanent enchantment = gd.playerBattlefields.get(player2.getId()).getLast();

        castKnight(1, enchantment.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Destroy mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getLast();

        assertThatThrownBy(() -> castKnight(1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("ETB mode gains 4 life")
    void lifeMode() {
        harness.setLife(player1, 10);

        castKnight(2);
        resolveCreatureAndEtb();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    private void castKnight(int mode) {
        castKnight(mode, null);
    }

    private void castKnight(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new KnightOfAutumn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
