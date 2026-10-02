package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchitectOfTheUntamed.class, Forest.class})
class ArchitectOfTheUntamedTest extends BaseCardTest {

    @Test
    void landfallGivesOneEnergy() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysEightEnergyToCreateSixSixColorlessBeastArtifactCreatureToken() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().isToken()).isTrue();
        assertThat(beast.getEffectivePower()).isEqualTo(6);
        assertThat(beast.getEffectiveToughness()).isEqualTo(6);
        assertThat(beast.getCard().getColor()).isNull();
        assertThat(beast.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    void cannotActivateWithoutEightEnergy() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");
    }

    @Test
    void opponentsLandDoesNotGiveEnergy() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void eachLandEnteringWithoutBeingPlayedGivesEnergy() {
        addCreatureReady(player1, new ArchitectOfTheUntamed());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndPaysBeforeResolution() {
        Permanent architect = harness.addToBattlefieldAndReturn(player1, new ArchitectOfTheUntamed());
        architect.setSummoningSick(true);
        architect.setTapped(true);
        gd.playerEnergyCounters.put(player1.getId(), 10);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(countPermanents(player1, "Beast")).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }
}
