package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RescueSkiff.class, GloriousAnthem.class, GrizzlyBears.class, HolyDay.class, Pacifism.class})
class RescueSkiffTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new RescueSkiff(), "{5}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsTargetEnchantmentFromGraveyard() {
        Card enchantment = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.castFromHand(player1, new RescueSkiff(), "{5}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(enchantment.getId()));
        harness.assertNotInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    void cannotTargetNonCreatureNonEnchantmentCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.castFromHand(player1, new RescueSkiff(), "{5}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(instant.getId()));
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    void stationUsesTappedCreaturePowerAndUnlocksFlyingAtTenCounters() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(skiff), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(skiff.getCounterCount(CounterType.CHARGE)).isEqualTo(3);

        skiff.setCounterCount(CounterType.CHARGE, 10);
        assertThat(gqs.isCreature(gd, skiff)).isTrue();
        assertThat(gqs.getEffectivePower(gd, skiff)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, skiff)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, skiff, Keyword.FLYING)).isTrue();
    }

    @Test
    void stationRequiresAnotherUntappedCreature() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(skiff), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnedAuraEntersAttachedToChosenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));

        harness.castFromHand(player1, new RescueSkiff(), "{5}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        harness.castFromHand(player1, new RescueSkiff(), "{5}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rescue Skiff");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stationCanTapSummoningSickCreatureWithoutTappingSkiff() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(skiff), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(skiff.isTapped()).isFalse();
        assertThat(skiff.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void animatedSkiffCannotStationItselfOrUseTappedOrOpposingCreatures() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());
        skiff.setCounterCount(CounterType.CHARGE, 10);
        skiff.setSummoningSick(false);
        Permanent tapped = addCreatureReady(player1, new GrizzlyBears());
        tapped.tap();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(skiff), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skiff.isTapped()).isFalse();
        assertThat(skiff.getCounterCount(CounterType.CHARGE)).isEqualTo(10);
    }

    @Test
    void stationCannotBeActivatedDuringCombat() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(skiff), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void stationUsesLastKnownPowerWhenTappedCreatureLeavesBeforeResolution() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(skiff), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(skiff.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void losingChargeCountersDisablesCreatureStatusAndFlying() {
        Permanent skiff = harness.addToBattlefieldAndReturn(player1, new RescueSkiff());
        skiff.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, skiff)).isFalse();
        assertThat(gqs.hasKeyword(gd, skiff, Keyword.FLYING)).isFalse();

        skiff.setCounterCount(CounterType.CHARGE, 10);
        assertThat(gqs.isCreature(gd, skiff)).isTrue();
        assertThat(gqs.hasKeyword(gd, skiff, Keyword.FLYING)).isTrue();

        skiff.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, skiff)).isFalse();
        assertThat(gqs.hasKeyword(gd, skiff, Keyword.FLYING)).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
