package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TwitchingDoll.class)
class TwitchingDollTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Doll without counters creates no tokens")
    void sacrificeWithoutCountersCreatesNoTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Twitching Doll");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Spiders have the specified characteristics and counters use last known information")
    void createsSpecifiedSpidersUsingSacrificedDollCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);
        doll.setCounterCount(CounterType.NEST, 2);
        doll.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Twitching Doll");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3).allSatisfy(spider -> {
            assertThat(spider.getCard().isToken()).isTrue();
            assertThat(spider.getCard().getName()).isEqualTo("Spider");
            assertThat(spider.getCard().getPower()).isEqualTo(2);
            assertThat(spider.getCard().getToughness()).isEqualTo(2);
            assertThat(spider.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(spider.getCard().getSubtypes()).contains(CardSubtype.SPIDER);
            assertThat(spider.getCard().getKeywords()).contains(Keyword.REACH);
            assertThat(spider.getTotalCounterCount()).isZero();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both tap abilities are unavailable while summoning sick")
    void summoningSicknessPreventsBothAbilities() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Twitching Doll");
        assertThat(doll.isTapped()).isFalse();
        assertThat(doll.getTotalCounterCount()).isZero();
    }

    @Test
    @DisplayName("Mana ability resolves without the stack and cannot be followed by sacrifice while tapped")
    void manaAbilityResolvesImmediatelyAndPreventsSacrificeWhileTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
        assertThat(doll.getCounterCount(CounterType.NEST)).isOne();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Twitching Doll");
    }

    @Test
    @DisplayName("Sacrifice requires an empty stack but the mana ability does not")
    void sacrificeCannotRespondToAnotherAbilityButManaAbilityCan() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        first.setSummoningSick(false);
        second.setSummoningSick(false);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isOne();
        assertThat(first.getCounterCount(CounterType.NEST)).isOne();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Mana ability adds mana and a nest counter")
    void manaAbilityAddsManaAndNestCounter() {
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(doll.getCounterCount(CounterType.NEST)).isEqualTo(1);
        assertThat(doll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability creates one Spider token per counter")
    void sacrificeAbilityCreatesSpidersForAllCounters() {
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);
        doll.setCounterCount(CounterType.NEST, 2);
        doll.setCounterCount(CounterType.CHARGE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Twitching Doll");
        harness.assertInGraveyard(player1, "Twitching Doll");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated as a sorcery")
    void sacrificeAbilityIsSorcerySpeed() {
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new TwitchingDoll());
        doll.setSummoningSick(false);
        doll.setCounterCount(CounterType.NEST, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
