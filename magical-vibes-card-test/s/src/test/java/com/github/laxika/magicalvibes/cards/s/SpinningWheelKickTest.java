package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
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

@CardUsed({SpinningWheelKick.class, HillGiant.class, GrizzlyBears.class, NicolBolasPlaneswalker.class, FangOfShigeki.class})
class SpinningWheelKickTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen creature deals its power to each creature and planeswalker target")
    void dealsPowerToEachCreatureAndPlaneswalkerTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 2, List.of(source.getId(), bear.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Nicol Bolas, Planeswalker");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The source creature may also be a damage target")
    void sourceMayBeTargeted() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 1, List.of(source.getId(), source.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Only a creature you control can be chosen as the source")
    void sourceMustBeControlledCreature() {
        Permanent opponentSource = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, List.of(opponentSource.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Players are not legal damage targets")
    void playerIsNotLegalTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    @Test
    void requiresExactlyXDamageTargets() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 2, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageTargetsMustBeDistinctWithinTheirGroup() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 2, List.of(source.getId(), target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXStillTargetsTheSourceButDealsNoDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0, List.of(source.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Spinning Wheel Kick");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void bothXSymbolsMustBePaid() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 2, List.of(source.getId(), target.getId(), otherTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesSourcePowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 1, List.of(source.getId(), target.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void removedSourceDealsNoDamageEvenWithLegalVictim() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 1, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Spinning Wheel Kick");
    }

    @Test
    void sourceNoLongerControlledByCasterDealsNoDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 1, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void remainingLegalVictimStillTakesDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent removedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remainingTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 2,
                List.of(source.getId(), removedTarget.getId(), remainingTarget.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removedTarget);
        gd.playerHands.get(player2.getId()).add(removedTarget.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(removedTarget.getMarkedDamage()).isZero();
    }

    @Test
    void lethalDamageToSourceDoesNotStopDamageToOtherVictims() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 2,
                List.of(source.getId(), source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void damageUsesTheChosenCreaturesDeathtouch() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new FangOfShigeki());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpinningWheelKick()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 2,
                List.of(source.getId(), target.getId(), otherTarget.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fang of Shigeki");
        assertThat(source.getMarkedDamage()).isZero();
    }
}
