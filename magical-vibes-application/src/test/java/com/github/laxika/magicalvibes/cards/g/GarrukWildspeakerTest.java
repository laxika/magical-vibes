package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({GarrukWildspeaker.class, Forest.class, Mountain.class, RuneclawBear.class})
class GarrukWildspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new GarrukWildspeaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        Permanent garruk = findPermanent(player1, "Garruk Wildspeaker");
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 untaps two target tapped lands")
    void plusOneUntapsTwoTargetLands() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 can target untapped lands (no-op but still gains loyalty)")
    void plusOneCanTargetUntappedLands() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 can target opponent's lands")
    void plusOneCanTargetOpponentsLands() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent ownForest = addForest(player1);
        Permanent oppForest = addForest(player2);
        ownForest.tap();
        oppForest.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(ownForest.getId(), oppForest.getId()));
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(ownForest.isTapped()).isFalse();
        assertThat(oppForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 can target different land types")
    void plusOneCanTargetDifferentLandTypes() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest = addForest(player1);
        Permanent mountain = addMountain(player1);
        forest.tap();
        mountain.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId(), mountain.getId()));
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("-1 creates a 3/3 green Beast token")
    void minusOneCreatesBeastToken() {
        Permanent garruk = addReadyGarruk(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        Permanent token = findPermanent(player1, "Beast");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("-1 can be used multiple turns to create multiple tokens")
    void minusOneCreatesMultipleTokensOverTurns() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 5);

        // First activation
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        // Second activation
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        long tokenCount = countPermanents(player1, "Beast");
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("-4 gives +3/+3 and trample to controlled creatures until end of turn")
    void minusFourBoostsAndGrantsTrample() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 7);

        Permanent creaturePerm = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        // Runeclaw Bear is a 2/2 without trample.
        assertThat(gqs.getEffectivePower(gd, creaturePerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creaturePerm)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creaturePerm, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("-4 does not affect opponent's creatures")
    void minusFourDoesNotAffectOpponentCreatures() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 7);

        Permanent oppPerm = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // The opponent's Runeclaw Bear is unaffected.
        assertThat(oppPerm.getEffectivePower()).isEqualTo(2);
        assertThat(oppPerm.getEffectiveToughness()).isEqualTo(2);
        assertThat(oppPerm.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate -4 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyGarruk(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Garruk dies when -4 brings loyalty to 0 (from starting 4)")
    void minusFourWithFourLoyaltyKillsGarruk() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("+1 requires two distinct lands and does not pay loyalty for invalid targets")
    void plusOneRequiresTwoDistinctLands() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest = addForest(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(forest.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+1 cannot target a nonland creature")
    void plusOneRejectsNonland() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest = addForest(player1);
        Permanent bear = addCreatureReady(player1, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(forest.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+1 still untaps the remaining land when one target leaves")
    void plusOneResolvesWithOneRemainingTarget() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent first = addForest(player1);
        Permanent second = addForest(player1);
        first.tap();
        second.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isFalse();
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only one loyalty ability may be activated per turn")
    void cannotActivateAnotherLoyaltyAbilityInSameTurn() {
        Permanent garruk = addReadyGarruk(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only one loyalty ability");

        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }

    @Test
    @DisplayName("-1 creates its Beast even when paying the cost kills Garruk")
    void minusOneWithOneLoyaltyStillCreatesToken() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Garruk Wildspeaker");
        harness.assertInGraveyard(player1, "Garruk Wildspeaker");
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }

    @Test
    @DisplayName("-4 expires at end of turn and does not affect creatures entering later")
    void ultimateExpiresAndDoesNotAffectLaterCreatures() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 7);
        Permanent original = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, later, Keyword.TRAMPLE)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed({SarkhanTheMasterless.class})
    @DisplayName("-4 grants trample to Garruk himself when he is a creature")
    void ultimateIncludesAnimatedGarruk() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 7);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, garruk)).isTrue();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, garruk)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, garruk)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, garruk, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("-4 still boosts creatures when its loyalty cost puts Garruk in the graveyard")
    void ultimateResolvesAfterGarrukDiesToItsCost() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setCounterCount(CounterType.LOYALTY, 4);
        Permanent bear = addCreatureReady(player1, new RuneclawBear());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Garruk Wildspeaker");
        harness.assertInGraveyard(player1, "Garruk Wildspeaker");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyGarruk(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addMountain(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Mountain());
    }
}
