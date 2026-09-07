package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SaltcrustedSteppe.class)
class SaltcrustedSteppeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        harness.addToBattlefield(player1, new SaltcrustedSteppe());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability pays {1} and puts a storage counter on the land")
    void firstAbilityAddsStorageCounter() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SaltcrustedSteppe());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(steppe.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(steppe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability removes storage counters and adds green and white mana")
    void secondAbilityAddsManaInAnyCombinationOfGreenAndWhite() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SaltcrustedSteppe());
        steppe.setCounterCount(CounterType.STORAGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "WHITE");

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(steppe.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(steppe.isTapped()).isTrue();
    }
}
