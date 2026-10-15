package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HELIOSOne.class, GrizzlyBears.class, HillGiant.class})
class HELIOSOneTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new HELIOSOne());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tapsToCreateAnEnergyCounter() {
        harness.addToBattlefield(player1, new HELIOSOne());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysXEnergyAndSacrificesToDestroyAPermanentWithManaValueX() {
        harness.addToBattlefield(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, 2, target.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertInGraveyard(player1, "HELIOS One");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetAPermanentWithManaValueAboveX() {
        harness.addToBattlefield(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "HELIOS One");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void cannotCreateEnergyWithoutPayingOneMana() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void cannotTargetAPermanentWithManaValueBelowX() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetALandEvenWithMatchingManaValue() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HELIOSOne());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "HELIOS One");
        harness.assertOnBattlefield(player2, "HELIOS One");
    }

    @Test
    void cannotDestroyWithoutEnoughEnergy() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "HELIOS One");
    }

    @Test
    void cannotDestroyWithoutThreeMana() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "HELIOS One");
    }

    @Test
    void canDestroyOwnPermanentAndKeepsUnspentEnergy() {
        harness.addToBattlefield(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, 2, target.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertInGraveyard(player1, "HELIOS One");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotDestroyOutsideMainPhase() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "HELIOS One");
    }

    @Test
    void cannotDestroyDuringOpponentsTurn() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "HELIOS One");
    }

    @Test
    void cannotDestroyWhileAnotherAbilityIsOnTheStack() {
        Permanent helios = harness.addToBattlefieldAndReturn(player1, new HELIOSOne());
        harness.addToBattlefield(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helios.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "HELIOS One");
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }
}
