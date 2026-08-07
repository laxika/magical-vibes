package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EiganjoCastleTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds {W}")
    void manaAbilityAddsWhite() {
        harness.addToBattlefield(player1, new EiganjoCastle());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent castle = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(castle.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shields a legendary creature from the next 2 damage")
    void shieldsLegendaryCreature() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        harness.activateAbility(player1, 0, 1, null, mirriId);
        harness.passBothPriorities();

        Permanent mirri = findPermanent(player1, "Mirri, Cat Warrior");
        assertThat(mirri.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("The shield absorbs 2 damage from a burn spell")
    void shieldAbsorbsBurnDamage() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        harness.activateAbility(player1, 0, 1, null, mirriId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mirriId);
        harness.passBothPriorities();

        Permanent mirri = findPermanent(player1, "Mirri, Cat Warrior");
        assertThat(mirri.getMarkedDamage()).isEqualTo(1);
        assertThat(mirri.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("A nonlegendary creature is not a legal target")
    void nonlegendaryCreatureIsIllegalTarget() {
        harness.addToBattlefield(player1, new EiganjoCastle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
