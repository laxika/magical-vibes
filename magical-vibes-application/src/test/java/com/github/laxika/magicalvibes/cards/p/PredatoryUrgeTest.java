package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PredatoryUrgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature taps to deal mutual power damage to a target creature")
    void enchantedCreatureFightsTargetCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new HillGiant());
        Permanent aura = new Permanent(new PredatoryUrge());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        Permanent target = addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(enchantedCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Granted ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new HillGiant());
        Permanent aura = new Permanent(new PredatoryUrge());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
