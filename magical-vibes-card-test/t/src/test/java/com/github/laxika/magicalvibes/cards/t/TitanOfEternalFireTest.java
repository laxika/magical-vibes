package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitanOfEternalFire.class, EliteVanguard.class, GrizzlyBears.class})
class TitanOfEternalFireTest extends BaseCardTest {

    @Test
    @DisplayName("Human creatures you control can pay red and tap to deal 1 damage")
    void humanYouControlGainsDamageAbility() {
        harness.setLife(player2, 20);
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new TitanOfEternalFire());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(human.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Titan of Eternal Fire grants the ability only to your Humans")
    void onlyOwnHumansGainAbility() {
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent nonHuman = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TitanOfEternalFire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");

        assertThat(human.isTapped()).isFalse();
        assertThat(nonHuman.isTapped()).isFalse();
    }

    @Test
    void opposingHumanDoesNotGainAbility() {
        Permanent human = addCreatureReady(player2, new EliteVanguard());
        harness.addToBattlefield(player1, new TitanOfEternalFire());
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(human.isTapped()).isFalse();
    }

    @Test
    void humanDealsDamageToCreatureAsTheDamageSource() {
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new TitanOfEternalFire());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.damageDealtThisTurnBySource.get(human.getId())).isEqualTo(1);
        assertThat(human.isTapped()).isTrue();
    }

    @Test
    void summoningSickHumanCannotPayTapCost() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new TitanOfEternalFire());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(human.isTapped()).isFalse();
    }

    @Test
    void humanCannotActivateWithoutRedMana() {
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new TitanOfEternalFire());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(human.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityOnStackResolvesAfterTitanLeavesButNoNewAbilityCanBeActivated() {
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new TitanOfEternalFire());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(titan);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        human.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
