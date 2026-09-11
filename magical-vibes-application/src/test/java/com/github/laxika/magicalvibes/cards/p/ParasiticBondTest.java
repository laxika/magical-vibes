package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParasiticBond.class, Forest.class, GorillaWarrior.class})
class ParasiticBondTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature with Parasitic Bond")
    void canEnchantCreature() {
        Permanent bears = addCreature(player2);

        harness.setHand(player1, List.of(new ParasiticBond()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Parasitic Bond attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreature(player2);
        ParasiticBond parasiticBond = new ParasiticBond();

        harness.setHand(player1, List.of(parasiticBond));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(parasiticBond.getId())
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        addCreature(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new ParasiticBond()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature's controller takes 2 damage at their upkeep")
    void enchantedControllerTakesDamageAtUpkeep() {
        Permanent bears = addCreature(player2);
        attachParasiticBond(bears);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Parasitic Bond does not damage its controller during their own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent bears = addCreature(player2);
        attachParasiticBond(bears);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Damage accumulates over multiple upkeeps")
    void damageAccumulatesOverUpkeeps() {
        Permanent bears = addCreature(player2);
        attachParasiticBond(bears);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    private void attachParasiticBond(Permanent creature) {
        Permanent parasiticBond = harness.addToBattlefieldAndReturn(player1, new ParasiticBond());
        parasiticBond.setAttachedTo(creature.getId());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GorillaWarrior());
    }
}
