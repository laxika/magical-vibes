package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(EiganjoCastle.class)
class EiganjoCastleTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds {W}")
    void manaAbilityAddsWhite() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(castle.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @CardUsed(MirriCatWarrior.class)
    @DisplayName("Shields a legendary creature from the next 2 damage")
    void shieldsLegendaryCreature() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, mirri.getId());
        harness.passBothPriorities();

        assertThat(mirri.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @CardUsed({MirriCatWarrior.class, LightningBolt.class})
    @DisplayName("The shield absorbs 2 damage from a burn spell")
    void shieldAbsorbsBurnDamage() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        Permanent mirri = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, mirri.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mirri.getId());
        harness.passBothPriorities();

        assertThat(mirri.getMarkedDamage()).isEqualTo(1);
        assertThat(mirri.getDamagePreventionShield()).isZero();
    }

    @Test
    @CardUsed(MirriCatWarrior.class)
    @DisplayName("The ability can target an opponent's legendary creature")
    void shieldsOpponentsLegendaryCreature() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        Permanent mirri = harness.addToBattlefieldAndReturn(player2, new MirriCatWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, mirri.getId());
        harness.passBothPriorities();

        assertThat(mirri.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("A legendary land is not a legal target")
    void legendaryLandIsIllegalTarget() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EiganjoCastle());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(GrizzlyBears.class)
    @DisplayName("A nonlegendary creature is not a legal target")
    void nonlegendaryCreatureIsIllegalTarget() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
