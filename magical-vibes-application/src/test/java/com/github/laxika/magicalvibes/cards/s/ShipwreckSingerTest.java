package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipwreckSingerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability forces an opponent's creature to attack without tapping Shipwreck Singer")
    void forcesOpponentsCreatureToAttack() {
        Permanent singer = addCreatureReady(player1, new ShipwreckSinger());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isMustAttackThisTurn()).isTrue();
        assertThat(singer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("First ability cannot target your own creature or a noncreature permanent")
    void firstAbilityRestrictsTargets() {
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Second ability gives -1/-1 to all attacking creatures only")
    void weakensAttackingCreatures() {
        addCreatureReady(player1, new ShipwreckSinger());
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player2, new GrizzlyBears());
        ownAttacker.setAttacking(true);
        opponentAttacker.setAttacking(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownAttacker.getEffectivePower()).isEqualTo(1);
        assertThat(ownAttacker.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentAttacker.getEffectivePower()).isEqualTo(1);
        assertThat(opponentAttacker.getEffectiveToughness()).isEqualTo(1);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonattacker.getEffectiveToughness()).isEqualTo(2);
    }
}
