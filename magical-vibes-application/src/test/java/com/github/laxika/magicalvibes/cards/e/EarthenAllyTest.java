package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AvatarAang;
import com.github.laxika.magicalvibes.cards.b.Badgermole;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.cards.w.WaterTribeCaptain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthenAlly.class, WaterTribeCaptain.class, Badgermole.class, OtterPenguin.class,
        AvatarAang.class, Forest.class})
class EarthenAllyTest extends BaseCardTest {

    @Test
    void getsPowerForDistinctColorsAmongAlliesYouControl() {
        Permanent earthenAlly = harness.addToBattlefieldAndReturn(player1, new EarthenAlly());
        harness.addToBattlefield(player1, new WaterTribeCaptain());
        harness.addToBattlefield(player1, new Badgermole());
        harness.addToBattlefield(player1, new OtterPenguin());

        assertThat(gqs.getEffectivePower(gd, earthenAlly)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, earthenAlly)).isEqualTo(2);
    }

    @Test
    void earthbendsTargetLandWithFiveCounters() {
        harness.addToBattlefield(player1, new EarthenAlly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addMana();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void cannotEarthbendLandControlledByOpponent() {
        harness.addToBattlefield(player1, new EarthenAlly());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEarthbendANonlandPermanent() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new EarthenAlly());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ally.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void earthbendStillResolvesAfterItsSourceLeaves() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new EarthenAlly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addMana();
        harness.activateAbility(player1, 0, null, land.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, ally);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void returningAnEarthbendedLandToHandDoesNotReturnItToTheBattlefield() {
        harness.addToBattlefield(player1, new EarthenAlly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addMana();
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToHand(gd, land);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void countsEachColorOfAMulticolorAllyOnlyOnceAndUpdatesWhenItLeaves() {
        Permanent earthenAlly = harness.addToBattlefieldAndReturn(player1, new EarthenAlly());
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AvatarAang());
        harness.addToBattlefield(player1, new EarthenAlly());

        assertThat(gqs.getEffectivePower(gd, earthenAlly)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earthenAlly)).isEqualTo(2);

        harness.getPermanentRemovalService().removePermanentToHand(gd, aang);

        assertThat(gqs.getEffectivePower(gd, earthenAlly)).isEqualTo(1);
    }

    @Test
    void ignoresColorsOfOpposingAllies() {
        Permanent earthenAlly = harness.addToBattlefieldAndReturn(player1, new EarthenAlly());
        harness.addToBattlefield(player2, new AvatarAang());

        assertThat(gqs.getEffectivePower(gd, earthenAlly)).isEqualTo(1);
    }

    @Test
    void repeatedEarthbendingAddsCountersToTheSameLand() {
        harness.addToBattlefield(player1, new EarthenAlly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addMana();
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        addMana();
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void earthbendedLandReturnsTappedAfterDyingOrBeingExiledEvenWithoutTheSource(boolean exile) {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new EarthenAlly());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addMana();
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToHand(gd, ally);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        if (exile) {
            harness.getPermanentRemovalService().removePermanentToExile(gd, land);
        } else {
            harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, land);
        }
        harness.passBothPriorities();

        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.getId()).isNotEqualTo(land.getId());
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returnedLand)).isTrue();
        assertThat(gqs.isCreature(gd, returnedLand)).isFalse();
        assertThat(returnedLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
