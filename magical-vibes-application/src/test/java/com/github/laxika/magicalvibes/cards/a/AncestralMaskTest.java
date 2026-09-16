package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BogSmugglers;
import com.github.laxika.magicalvibes.cards.v.VernalEquinox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestralMask.class, BogSmugglers.class, VernalEquinox.class})
class AncestralMaskTest extends BaseCardTest {

    @Test
    void doesNotCountItself() {
        Permanent creature = addCreatureReady(player1, new BogSmugglers());
        attachMask(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void getsTwoForEachOtherEnchantmentOnTheBattlefield() {
        Permanent creature = addCreatureReady(player1, new BogSmugglers());
        attachMask(creature);

        harness.addToBattlefield(player1, new VernalEquinox());
        harness.addToBattlefield(player2, new VernalEquinox());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    void canEnchantAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new BogSmugglers());
        harness.setHand(player1, List.of(new AncestralMask()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void boostUpdatesAsOtherEnchantmentsEnterAndLeave() {
        Permanent creature = addCreatureReady(player1, new BogSmugglers());
        attachMask(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.addToBattlefield(player1, new VernalEquinox());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Vernal Equinox"));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new VernalEquinox());
        harness.setHand(player1, List.of(new AncestralMask()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachMask(Permanent creature) {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new AncestralMask());
        mask.setAttachedTo(creature.getId());
        return mask;
    }
}
