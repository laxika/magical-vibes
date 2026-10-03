package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.ConstrictingTendrils;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeaconBehemoth.class, AvatarOfMight.class, GrizzlyBears.class,
        BoneSaw.class, ConstrictingTendrils.class})
class BeaconBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants vigilance to a target creature with power 5 or greater")
    void grantsVigilanceToBigCreature() {
        addCreatureReady(player1, new BeaconBehemoth());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BeaconBehemoth());
        Permanent avatar = addCreatureReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void cannotTargetSmallCreature() {
        addCreatureReady(player1, new BeaconBehemoth());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 5 or greater");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick, targeting itself at exactly five power")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent beacon = harness.addToBattlefieldAndReturn(player1, new BeaconBehemoth());
        beacon.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, beacon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beacon, Keyword.VIGILANCE)).isTrue();
        assertThat(beacon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can grant vigilance to an opponent's creature")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new BeaconBehemoth());
        Permanent target = addCreatureReady(player2, new BeaconBehemoth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granted vigilance allows attacking without tapping")
    void vigilancePreventsTappingWhenAttacking() {
        Permanent beacon = addCreatureReady(player1, new BeaconBehemoth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, beacon.getId());
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(beacon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without paying one mana")
    void cannotActivateWithoutMana() {
        Permanent beacon = addCreatureReady(player1, new BeaconBehemoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, beacon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, beacon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new BeaconBehemoth());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 5 or greater");

        assertThat(gqs.hasKeyword(gd, equipment, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Ability does not resolve if the target's power falls below five in response")
    void rechecksPowerAtResolution() {
        Permanent beacon = addCreatureReady(player1, new BeaconBehemoth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new ConstrictingTendrils()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, beacon.getId());
        harness.castInstant(player2, 0, beacon.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, beacon)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, beacon, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance remains if power falls below five after the ability resolves")
    void retainsVigilanceAfterPowerFalls() {
        Permanent beacon = addCreatureReady(player1, new BeaconBehemoth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ConstrictingTendrils()));

        harness.activateAbility(player1, 0, null, beacon.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, beacon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beacon)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, beacon, Keyword.VIGILANCE)).isTrue();
    }
}
