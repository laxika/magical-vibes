package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Boneknitter.class, GrizzlyBears.class, Shock.class})
class BoneknitterTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability grants a regeneration shield to a Zombie")
    void regeneratesTargetZombie() {
        harness.addToBattlefield(player1, new Boneknitter());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        zombie.getGrantedSubtypes().add(CardSubtype.ZOMBIE);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, zombie.getId());
        harness.passBothPriorities();

        assertThat(zombie.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield prevents lethal damage")
    void regenerationShieldPreventsDestruction() {
        harness.addToBattlefield(player1, new Boneknitter());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        zombie.getGrantedSubtypes().add(CardSubtype.ZOMBIE);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, zombie.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, zombie.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zombie);
        assertThat(zombie.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Zombie")
    void cannotTargetNonZombie() {
        harness.addToBattlefield(player1, new Boneknitter());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Zombie");
    }
}
