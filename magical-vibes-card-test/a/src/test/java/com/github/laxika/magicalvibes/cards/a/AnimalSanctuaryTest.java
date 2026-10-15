package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrowsingTyrannodon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnimalSanctuary.class, DrowsingTyrannodon.class, AlpineWatchdog.class})
class AnimalSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new AnimalSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on a listed creature type")
    void putsCounterOnListedCreatureType() {
        harness.addToBattlefield(player1, new AnimalSanctuary());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, dog.getId());
        harness.passBothPriorities();

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without a listed creature type")
    void cannotTargetUnlistedCreatureType() {
        harness.addToBattlefield(player1, new AnimalSanctuary());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new DrowsingTyrannodon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPutCounterOnOpponentsDogAndPaysCostsBeforeResolution() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new AnimalSanctuary());
        Permanent dog = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, dog.getId());

        assertThat(sanctuary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateCounterAbilityWithOnlyOneMana() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new AnimalSanctuary());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dog.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sanctuary.isTapped()).isFalse();
        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateCounterAbilityWhenSanctuaryIsTapped() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new AnimalSanctuary());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        sanctuary.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, dog.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new AnimalSanctuary());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, dog.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sanctuary);
        harness.passBothPriorities();

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
