package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinCannon.class, LlanowarElves.class, Forest.class})
class GoblinCannonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player and is sacrificed")
    void dealsDamageToPlayerAndIsSacrificed() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new GoblinCannon());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cannon);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cannon.getCard());
    }

    @Test
    @DisplayName("Multiple activations deal damage before the first resolution sacrifices it")
    void multipleActivationsEachDealDamage() {
        harness.addToBattlefield(player1, new GoblinCannon());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new GoblinCannon());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Does not sacrifice itself when its target becomes illegal")
    void doesNotSacrificeWhenTargetBecomesIllegal() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new GoblinCannon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cannon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cannon.getCard());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GoblinCannon());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice happens on resolution, not as an activation cost")
    void remainsOnBattlefieldUntilResolution() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new GoblinCannon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cannon);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Cannon");
        harness.assertInGraveyard(player1, "Goblin Cannon");
    }

    @Test
    @DisplayName("A tapped Cannon can activate and target its controller")
    void tappedCannonCanDamageItsController() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new GoblinCannon());
        cannon.tap();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertNotOnBattlefield(player1, "Goblin Cannon");
        harness.assertInGraveyard(player1, "Goblin Cannon");
    }

    @Test
    @DisplayName("Activation requires two mana")
    void cannotActivateWithOnlyOneMana() {
        Permanent cannon = harness.addToBattlefieldAndReturn(player1, new GoblinCannon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cannon);
    }

    @Test
    @DisplayName("An older activation does not sacrifice a different Cannon")
    void olderActivationDoesNotSacrificeAnotherCannon() {
        harness.addToBattlefield(player1, new GoblinCannon());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoblinCannon());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

}
