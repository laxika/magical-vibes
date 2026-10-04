package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArchangelElspeth;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinBombardment.class, InvasionOfZendikar.class,
        ArchangelElspeth.class, MoggFanatic.class})
class GoblinBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 1 damage to a player with no mana cost")
    void dealsOneDamageToPlayer() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Deals 1 damage to a creature, killing a 1-toughness one")
    void dealsOneDamageToCreature() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent fanatic = harness.addToBattlefieldAndReturn(player2, new MoggFanatic());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, fanatic.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mogg Fanatic");
    }

    @Test
    @DisplayName("Deals 1 damage to a planeswalker")
    void dealsOneDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ArchangelElspeth());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, elspeth.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals 1 damage to a battle")
    void dealsOneDamageToBattle() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, battle.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid before the damage ability resolves")
    void sacrificesBeforeResolution() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        Permanent fanatic = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());
        fanatic.tap();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Mogg Fanatic");
        harness.assertNotOnBattlefield(player1, "Mogg Fanatic");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Goblin Bombardment");
    }

    @Test
    @DisplayName("Can target the creature sacrificed to pay the cost, but the ability then has no legal target")
    void canTargetSacrificedCreature() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        Permanent fanatic = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());

        harness.activateAbility(player1, 0, null, fanatic.getId());

        harness.assertInGraveyard(player1, "Mogg Fanatic");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotPayWithOpponentsCreature() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        harness.addToBattlefield(player2, new MoggFanatic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Mogg Fanatic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chooses one creature to sacrifice and can activate again without tapping")
    void choosesCreatureAndActivatesRepeatedly() {
        harness.addToBattlefield(player1, new GoblinBombardment());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MoggFanatic());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Mogg Fanatic");
        harness.assertOnBattlefield(player1, "Goblin Bombardment");
    }

    @Test
    @DisplayName("Cannot target a noncreature enchantment")
    void cannotTargetNoncreatureEnchantment() {
        Permanent bombardment = harness.addToBattlefieldAndReturn(player1, new GoblinBombardment());
        harness.addToBattlefield(player1, new MoggFanatic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bombardment.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mogg Fanatic");
        assertThat(gd.stack).isEmpty();
    }
}
