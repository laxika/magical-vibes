package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GatesOfIstfell;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvalancheCaller.class, SnowCoveredForest.class, GatesOfIstfell.class})
class AvalancheCallerTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a snow land you control into a 4/4 Elemental with hexproof and haste")
    void animatesSnowLandYouControl() {
        addCallerReady(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.isLand(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(4);
        assertThat(snowLand.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        addCallerReady(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
        assertThat(gqs.isLand(gd, snowLand)).isTrue();
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a snow land controlled by an opponent")
    void cannotTargetOpponentsSnowLand() {
        addCallerReady(player1);
        Permanent snowLand = addSnowLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonsnow land")
    void cannotTargetNonsnowLand() {
        addCallerReady(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new GatesOfIstfell());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addCallerReady(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AvalancheCaller());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent caller = harness.addToBattlefieldAndReturn(player1, new AvalancheCaller());
        caller.setSummoningSick(true);
        caller.setTapped(true);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(4);
        assertThat(caller.isTapped()).isTrue();
    }

    @Test
    void resolvesAfterCallerLeavesBattlefield() {
        Permanent caller = addCallerReady(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        gd.playerBattlefields.get(player1.getId()).remove(caller);
        gd.playerGraveyards.get(player1.getId()).add(caller.getCard());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void canAnimateAnAlreadyAnimatedSnowLand() {
        addCallerReady(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotAnimateLandThatChangesControllerBeforeResolution() {
        addCallerReady(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        gd.playerBattlefields.get(player1.getId()).remove(snowLand);
        gd.playerBattlefields.get(player2.getId()).add(snowLand);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        addCallerReady(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, snowLand)).isFalse();
    }

    private Permanent addCallerReady(Player player) {
        Permanent caller = harness.addToBattlefieldAndReturn(player, new AvalancheCaller());
        caller.setSummoningSick(false);
        return caller;
    }

    private Permanent addSnowLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SnowCoveredForest());
    }
}
