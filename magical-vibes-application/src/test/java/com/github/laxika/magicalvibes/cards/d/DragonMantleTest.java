package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Dragon Mantle attaches to a creature and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.setHand(player1, List.of(new DragonMantle()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Enchanted creature can activate {R}: +1/+0")
    void grantedAbilityBoostsEnchantedCreature() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new DragonMantle());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dragon Mantle cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new DragonMantle()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
