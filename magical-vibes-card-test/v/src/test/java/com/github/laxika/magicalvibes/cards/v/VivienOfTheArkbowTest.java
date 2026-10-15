package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.DaggerbackBasilisk;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VivienOfTheArkbow.class, GreenwoodSentinel.class, ColossalDreadmaw.class,
        DaggerbackBasilisk.class, Shock.class, TitanicGrowth.class, SarkhanTheMasterless.class})
class VivienOfTheArkbowTest extends BaseCardTest {

    @Test
    @DisplayName("+2 puts two +1/+1 counters on up to one target creature")
    void plusTwoPutsCountersOnTargetCreature() {
        Permanent vivien = addReadyVivien(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("+2 may be activated without choosing a creature")
    void plusTwoMayChooseNoCreature() {
        Permanent vivien = addReadyVivien(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0);
    }

    @Test
    @DisplayName("-3 makes a controlled creature deal its power to an opponent's creature")
    void minusThreeDealsControlledCreaturePowerToOpponentCreature() {
        Permanent vivien = addReadyVivien(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isEqualTo(0);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("-9 boosts the controller's creatures and grants trample until end of turn")
    void minusNineBoostsOwnCreaturesAndGrantsTrample() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 9);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void plusTwoCanTargetAnOpponentsCreature() {
        addReadyVivien(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void plusTwoKeepsLoyaltyWhenTargetDiesInResponse() {
        Permanent vivien = addReadyVivien(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void minusThreeUsesPowerAtResolution() {
        addReadyVivien(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), target.getId()));
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    void minusThreeDealsNoDamageWhenSourceDiesInResponse() {
        addReadyVivien(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), target.getId()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    void minusThreeDealsNoDamageWhenRecipientDiesInResponse() {
        addReadyVivien(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), target.getId()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void minusThreeRejectsAnOpponentsCreatureAsTheDamageSource() {
        Permanent vivien = addReadyVivien(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(source.getId(), target.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusThreeUsesTheCreaturesDeathtouch() {
        addReadyVivien(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DaggerbackBasilisk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Daggerback Basilisk");
    }

    @Test
    void minusThreeRejectsAnOwnCreatureAsTheDamageRecipient() {
        Permanent vivien = addReadyVivien(player1);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(source.getId(), target.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(vivien.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusNineDoesNotAffectCreaturesEnteringLaterAndExpiresAtCleanup() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 9);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GreenwoodSentinel());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Vivien of the Arkbow");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed({VivienOfTheArkbow.class, SarkhanTheMasterless.class})
    void minusNineGrantsTrampleToVivienWhenSheIsACreature() {
        Permanent vivien = addReadyVivien(player1);
        vivien.setCounterCount(CounterType.LOYALTY, 10);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vivien)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, vivien)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, vivien, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyVivien(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VivienOfTheArkbow());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
