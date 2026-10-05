package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChandraHeartOfFire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafkinAvenger.class, AvatarOfMight.class, GrizzlyBears.class, ChandraHeartOfFire.class})
class LeafkinAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap adds green mana for each creature with power 4 or greater you control")
    void tapAddsGreenManaForBigCreatures() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new LeafkinAvenger());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());

        avenger.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven generic and red mana makes the Avenger deal damage equal to its power")
    void dealsPowerDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LeafkinAvenger());

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Damage ability cannot target a creature")
    void damageAbilityCannotTargetCreature() {
        harness.addToBattlefield(player1, new LeafkinAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaCountsOnlyControlledCreaturesAndUsesCurrentPower() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new LeafkinAvenger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LeafkinAvenger());
        harness.addToBattlefield(player2, new LeafkinAvenger());
        avenger.setSummoningSick(false);
        other.setPowerModifier(-1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(avenger.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityCanProduceZeroMana() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new LeafkinAvenger());
        avenger.setSummoningSick(false);
        avenger.setPowerModifier(-1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(avenger.isTapped()).isTrue();
    }

    @Test
    void summoningSicknessPreventsManaActivation() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new LeafkinAvenger());
        avenger.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(avenger.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void damageUsesPowerAtResolutionAndDoesNotRequireTapping() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new LeafkinAvenger());
        avenger.setSummoningSick(true);
        avenger.setTapped(true);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        avenger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void damageCanTargetPlaneswalker() {
        harness.addToBattlefield(player1, new LeafkinAvenger());
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraHeartOfFire());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageCanTargetItsController() {
        harness.addToBattlefield(player1, new LeafkinAvenger());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }
}
