package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriestOfIroasTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndDestroysTargetEnchantment() {
        Permanent priest = addReadyPriest(player1);
        Permanent target = addReadyEnchantment(player2);
        addActivationMana();

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Priest of Iroas");
        harness.assertInGraveyard(player1, "Priest of Iroas");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void cannotTargetCreature() {
        addReadyPriest(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.forceActivePlayer(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPriest(com.github.laxika.magicalvibes.model.Player player) {
        Permanent priest = new Permanent(new PriestOfIroas());
        priest.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(priest);
        return priest;
    }

    private Permanent addReadyEnchantment(com.github.laxika.magicalvibes.model.Player player) {
        Permanent enchantment = new Permanent(new AngelicChorus());
        gd.playerBattlefields.get(player.getId()).add(enchantment);
        return enchantment;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
