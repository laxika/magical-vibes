package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishVatkeeper.class})
class ElvishVatkeeperTest extends BaseCardTest {

    @Test
    void incubatesAndTransformsAnIncubatorWhileDoublingItsCounters() {
        harness.castFromHand(player1, new ElvishVatkeeper(), "{1}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        Permanent vatkeeper = findPermanent(player1, "Elvish Vatkeeper");
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vatkeeper), null, incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(4);
    }

    @Test
    void incubatedTokenHasTheIncubatorArtifactSubtype() {
        harness.castFromHand(player1, new ElvishVatkeeper(), "{1}{B}{G}");
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCard().getSubtypes())
                .extracting(Enum::name).contains("INCUBATOR");
    }

    @Test
    void tokenCanTransformWithItsOwnAbilityWithoutDoublingCounters() {
        harness.castFromHand(player1, new ElvishVatkeeper(), "{1}{B}{G}");
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
    }

    @Test
    void transformingTargetInResponseMakesVatkeeperAbilityFizzle() {
        harness.castFromHand(player1, new ElvishVatkeeper(), "{1}{B}{G}");
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        Permanent vatkeeper = findPermanent(player1, "Elvish Vatkeeper");
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vatkeeper), null, incubator.getId());
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetTheVatkeeperItself() {
        Permanent vatkeeper = harness.addToBattlefieldAndReturn(player1, new ElvishVatkeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vatkeeper.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
