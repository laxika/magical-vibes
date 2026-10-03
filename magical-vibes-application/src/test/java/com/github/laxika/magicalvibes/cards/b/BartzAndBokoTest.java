package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NerivHeartOfTheStorm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BartzAndBoko.class, AvenInitiate.class, AirElemental.class, GrizzlyBears.class,
        NerivHeartOfTheStorm.class})
class BartzAndBokoTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Birds reduces Bartz and Boko's generic mana cost")
    void affinityForBirdsReducesGenericCost() {
        harness.addToBattlefield(player1, new AvenInitiate());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Other Birds deal their power as damage to the targeted opposing creature")
    void otherBirdsDealPowerDamageToTargetCreature() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenInitiate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(gqs.getEffectivePower(gd, bird));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("The ETB cannot target a creature controlled by Bartz and Boko's controller")
    void etbCannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void affinityDoesNotCountOpposingBirdsOrOwnNonBirds() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AvenInitiate());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void affinityCanRemoveAllGenericMana() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new AvenInitiate());
        }
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void affinityCannotRemoveColoredMana() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new AvenInitiate());
        }
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noOtherBirdsMeansNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void birdsAreDeterminedWhenTheTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new AvenInitiate());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void birdPowerIsDeterminedWhenTheTriggerResolves() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenInitiate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NerivHeartOfTheStorm());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        bird.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void multipleBirdsEachDealDamageToTheSameTarget() {
        harness.addToBattlefield(player1, new AvenInitiate());
        harness.addToBattlefield(player1, new AvenInitiate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NerivHeartOfTheStorm());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player2, "Neriv, Heart of the Storm");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @CardUsed({BartzAndBoko.class, AvenInitiate.class, AirElemental.class, NerivHeartOfTheStorm.class})
    void damageMultipliersUseEachBirdRatherThanBartzAndBokoAsTheSource() {
        harness.addToBattlefield(player1, new NerivHeartOfTheStorm());
        harness.addToBattlefield(player1, new AvenInitiate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BartzAndBoko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
}
