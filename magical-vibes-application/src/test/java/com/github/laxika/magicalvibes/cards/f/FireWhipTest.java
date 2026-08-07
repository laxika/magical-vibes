package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FireWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to a player")
    void grantedAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new FireWhip());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Aura deals 1 damage to a player and puts it in the graveyard")
    void sacrificeAbilityDealsDamage() {
        harness.setLife(player2, 20);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new FireWhip());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Fire Whip");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Granted ability kills a 1-toughness creature")
    void grantedAbilityKillsOneToughnessCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new FireWhip());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        Permanent elf = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elf);

        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Creature loses the granted ability when Fire Whip leaves the battlefield")
    void abilityGoesAwayWhenAuraRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new FireWhip());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Fire Whip can only enchant a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent enemyBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(enemyBears);

        harness.setHand(player1, List.of(new FireWhip()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
