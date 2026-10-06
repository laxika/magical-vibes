package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FanaticalDevotion;
import com.github.laxika.magicalvibes.cards.p.ParallaxDementia;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SaprolingBurst.class, SealOfCleansing.class, FanaticalDevotion.class, ParallaxDementia.class})
class SaprolingBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven fade counters")
    void entersWithSevenFadeCounters() {
        harness.castFromHand(player1, new SaprolingBurst(), "{4}{G}");
        harness.passBothPriorities();

        Permanent burst = findPermanent(player1, "Saproling Burst");
        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Removing fade counters creates tokens whose size tracks the Burst")
    void createsTokensWithLinkedPowerAndToughness() {
        Permanent burst = addBurstWithFadeCounters(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(2);
        Permanent firstSaproling = findPermanents(player1, "Saproling").getFirst();
        assertThat(firstSaproling.getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(firstSaproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, firstSaproling)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Saproling")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Fading removes a fade counter at upkeep and sacrifices the Burst at zero")
    void fadingAtUpkeep() {
        Permanent burst = addBurstWithFadeCounters(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(burst.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Saproling Burst");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Saproling Burst");
    }

    @Test
    @DisplayName("The activated ability cannot remove a fade counter when none remain")
    void cannotActivateWithoutFadeCounter() {
        addBurstWithFadeCounters(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creating a token with the last fade counter leaves a zero-toughness token to die")
    void lastFadeCounterCreatesZeroToughnessToken() {
        Permanent burst = addBurstWithFadeCounters(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(burst.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Saproling Burst");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Leaving the battlefield destroys tokens created with the Burst")
    void leavingBattlefieldDestroysCreatedTokens() {
        Permanent burst = addBurstWithFadeCounters(3);
        harness.addToBattlefield(player1, new SealOfCleansing());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);

        harness.activateAbility(player1, 1, null, burst.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Saproling Burst");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Fading does not remove counters during the opponent's upkeep")
    void doesNotFadeDuringOpponentsUpkeep() {
        Permanent burst = addBurstWithFadeCounters(3);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each token tracks only the Burst that created it")
    void separateBurstsKeepIndependentTokens() {
        Permanent firstBurst = addBurstWithFadeCounters(3);
        Permanent secondBurst = addBurstWithFadeCounters(6);
        harness.addToBattlefield(player1, new SealOfCleansing());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent firstToken = findPermanents(player1, "Saproling").getFirst();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        Permanent secondToken = findPermanents(player1, "Saproling").getLast();

        assertThat(gqs.getEffectivePower(gd, firstToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondToken)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondToken)).isEqualTo(5);

        harness.activateAbility(player1, 2, null, firstBurst.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Saproling")).containsExactly(secondToken);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondBurst);
        assertThat(gqs.getEffectivePower(gd, secondToken)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, secondToken)).isEqualTo(5);
    }

    @Test
    @DisplayName("An activation resolving after the Burst leaves creates a token that dies at zero toughness")
    void activationResolvesAfterSourceLeaves() {
        Permanent burst = addBurstWithFadeCounters(3);
        harness.addToBattlefield(player1, new SealOfCleansing());

        harness.activateAbility(player1, 0, null, null);
        assertThat(burst.getCounterCount(CounterType.FADE)).isEqualTo(2);
        harness.activateAbility(player1, 1, null, burst.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Saproling Burst");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaving the battlefield destroys a boosted Saproling even with a regeneration shield")
    void leavingBattlefieldPreventsRegeneration() {
        Permanent burst = addBurstWithFadeCounters(4);
        harness.addToBattlefield(player1, new SealOfCleansing());
        harness.addToBattlefield(player1, new FanaticalDevotion());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent protectedToken = findPermanents(player1, "Saproling").getFirst();
        Permanent fodder = findPermanents(player1, "Saproling").getLast();

        harness.setHand(player1, List.of(new ParallaxDementia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, protectedToken.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, protectedToken)).isEqualTo(4);

        harness.activateAbility(player1, 2, null, protectedToken.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        assertThat(protectedToken.getRegenerationShield()).isEqualTo(1);

        harness.activateAbility(player1, 1, null, burst.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Saproling Burst");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    private Permanent addBurstWithFadeCounters(int count) {
        Permanent burst = harness.addToBattlefieldAndReturn(player1, new SaprolingBurst());
        burst.setCounterCount(CounterType.FADE, count);
        return burst;
    }
}
