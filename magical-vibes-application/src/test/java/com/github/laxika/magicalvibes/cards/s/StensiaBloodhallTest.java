package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.v.VillageCannibals;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StensiaBloodhall.class, LilianaOfTheVeil.class, VillageCannibals.class})
class StensiaBloodhallTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not use the stack")
    void tapForColorlessAddsMana() {
        Permanent bloodhall = addReadyBloodhall(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(bloodhall.isTapped()).isTrue();
        // Mana ability — does not use the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage ability puts entry on the stack targeting a player")
    void damageAbilityGoesOnStack() {
        Permanent bloodhall = addReadyBloodhall(player1);
        addDamageMana();

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(bloodhall.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Damage ability deals 2 damage to target player on resolution")
    void dealsDamageToPlayer() {
        addReadyBloodhall(player1);
        addDamageMana();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage ability can target controller")
    void dealsDamageToSelf() {
        addReadyBloodhall(player1);
        addDamageMana();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent bloodhall = addReadyBloodhall(player1);
        bloodhall.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate damage ability without enough mana")
    void cannotActivateDamageAbilityWithoutMana() {
        addReadyBloodhall(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage removes two loyalty counters from a planeswalker")
    void dealsDamageToPlaneswalker() {
        addReadyBloodhall(player1);
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        liliana.setCounterCount(CounterType.LOYALTY, 3);
        addDamageMana();

        harness.activateAbility(player1, 0, 1, null, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(liliana);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage ability cannot target a creature")
    void cannotTargetCreature() {
        addReadyBloodhall(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VillageCannibals());
        addDamageMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player or planeswalker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage ability cannot target a land")
    void cannotTargetLand() {
        addReadyBloodhall(player1);
        Permanent land = addReadyBloodhall(player2);
        addDamageMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player or planeswalker");
    }

    @Test
    @DisplayName("Removing the source does not stop its activated damage ability")
    void damageResolvesAfterSourceLeaves() {
        Permanent bloodhall = addReadyBloodhall(player1);
        addDamageMana();
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bloodhall);
        gd.playerGraveyards.get(player1.getId()).add(bloodhall.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage ability does not resolve against a planeswalker that left the battlefield")
    void damageDoesNotResolveAfterTargetLeaves() {
        addReadyBloodhall(player1);
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        liliana.setCounterCount(CounterType.LOYALTY, 3);
        addDamageMana();
        harness.activateAbility(player1, 0, 1, null, liliana.getId());
        gd.playerBattlefields.get(player2.getId()).remove(liliana);
        gd.playerGraveyards.get(player2.getId()).add(liliana.getCard());

        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Five colorless mana cannot pay the damage ability's colored costs")
    void cannotPayColoredCostsWithColorlessMana() {
        Permanent bloodhall = addReadyBloodhall(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bloodhall.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Bloodhall cannot activate its damage ability even with enough mana")
    void cannotActivateDamageAbilityWhenTapped() {
        Permanent bloodhall = addReadyBloodhall(player1);
        bloodhall.tap();
        addDamageMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    private void addDamageMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addReadyBloodhall(Player player) {
        return harness.addToBattlefieldAndReturn(player, new StensiaBloodhall());
    }
}
