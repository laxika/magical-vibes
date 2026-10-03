package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DomrisAmbush.class, AirElemental.class, GrizzlyBears.class, JayaVeneratedFiremage.class})
class DomrisAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the first target before it deals damage equal to its power")
    void counterThenPowerDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DomrisAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(sourceId, target.getId()));

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target an opposing planeswalker")
    void canTargetOpposingPlaneswalker() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        harness.setHand(player1, List.of(new DomrisAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(sourceId, planeswalker.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void firstTargetMustBeControlled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentSource = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DomrisAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(opponentSource.getId(), opponentTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void secondTargetMustNotBeControlled() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new DomrisAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    @Test
    @DisplayName("Still puts the counter on its creature when the opposing target leaves")
    void opposingTargetLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DomrisAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals no damage when its creature leaves before resolution")
    void sourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DomrisAmbush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
