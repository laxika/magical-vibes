package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MizziumMortars.class, AvatarOfMight.class, GrizzlyBears.class, RubblebackRhino.class})
class MizziumMortarsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature you don't control")
    void dealsFourDamageToTarget() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Kills a creature with toughness 4 or less")
    void killsSmallCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Overloaded, it damages every creature you don't control and needs no target")
    void overloadDamagesEveryCreatureYouDontControl() {
        Permanent first = addCreatureReady(player2, new AvatarOfMight());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent own = addCreatureReady(player1, new AvatarOfMight());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(second);
        assertThat(own.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal casting cannot target an opposing hexproof creature")
    void normalCastCannotTargetHexproofCreature() {
        Permanent target = addCreatureReady(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Overload damages opposing hexproof creatures without damaging players")
    void overloadIgnoresHexproof() {
        Permanent target = addCreatureReady(player2, new RubblebackRhino());
        Permanent own = addCreatureReady(player1, new RubblebackRhino());
        int ownLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Rubbleback Rhino");
        assertThat(own.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
        harness.assertInGraveyard(player1, "Mizzium Mortars");
    }

    @Test
    @DisplayName("Overload can be cast when there are no opposing creatures")
    void overloadWithNoOpposingCreatures() {
        Permanent own = addCreatureReady(player1, new RubblebackRhino());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mizzium Mortars");
    }

    @Test
    @DisplayName("Normal casting deals no damage if its target leaves before resolution")
    void missingTargetDoesNotDamageOtherCreatures() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent other = addCreatureReady(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new MizziumMortars()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Mizzium Mortars");
    }
}