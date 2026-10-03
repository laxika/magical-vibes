package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.cards.v.VaultPlunderer;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneyardDesecrator.class, SterlingHound.class, VaultPlunderer.class, Xenograft.class})
class BoneyardDesecratorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Boneyard Desecrator")
    void sacrificingAnotherCreaturePutsCounterOnSource() {
        Permanent desecrator = addReadyDesecrator();
        harness.addToBattlefield(player1, new SterlingHound());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Sterling Hound");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"ASSASSIN", "MERCENARY", "PIRATE", "ROGUE", "WARLOCK"})
    @DisplayName("Sacrificing an outlaw also creates a Treasure token")
    void sacrificingAnOutlawCreatesTreasure(CardSubtype outlawSubtype) {
        Permanent desecrator = addReadyDesecrator();
        SterlingHound outlaw = new SterlingHound();
        outlaw.setSubtypes(List.of(outlawSubtype));
        harness.addToBattlefield(player1, outlaw);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Boneyard Desecrator cannot sacrifice itself")
    void cannotSacrificeItself() {
        addReadyDesecrator();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature made into an outlaw by Xenograft creates Treasure when sacrificed")
    void usesSacrificedCreaturesLastBattlefieldTypes() {
        Permanent desecrator = addReadyDesecrator();
        harness.addToBattlefield(player1, new SterlingHound());
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.ROGUE);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sterling Hound");
        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The sacrifice is paid immediately, while the counter and Treasure wait for resolution")
    void paysSacrificeBeforeResolvingAbility() {
        Permanent desecrator = addReadyDesecrator();
        harness.addToBattlefield(player1, new VaultPlunderer());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Vault Plunderer");
        harness.assertNotOnBattlefield(player1, "Vault Plunderer");
        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The ability can be activated while Boneyard Desecrator is tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent desecrator = harness.addToBattlefieldAndReturn(player1, new BoneyardDesecrator());
        desecrator.setSummoningSick(true);
        desecrator.setTapped(true);
        harness.addToBattlefield(player1, new BoneyardDesecrator());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(desecrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Boneyard Desecrator")).containsExactly(desecrator);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("An opposing creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addReadyDesecrator();
        harness.addToBattlefield(player2, new VaultPlunderer());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Vault Plunderer");
    }

    private Permanent addReadyDesecrator() {
        Permanent desecrator = harness.addToBattlefieldAndReturn(player1, new BoneyardDesecrator());
        desecrator.setSummoningSick(false);
        return desecrator;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
