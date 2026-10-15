package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MutableExplorer.class})
class MutableExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one tapped Mutavault token")
    void etbCreatesTappedMutavaultToken() {
        Permanent token = castAndGetToken();

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Mutavault");
        assertThat(token.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mutavault token taps for one colorless mana")
    void mutavaultTokenTapsForColorlessMana() {
        Permanent token = castAndGetToken();
        token.untap();

        gs.tapPermanent(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(token));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mutavault token can become a 2/2 creature with all creature types")
    void mutavaultTokenCanAnimate() {
        Permanent token = castAndGetToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.CHANGELING)).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.LAND);
    }

    @Test
    @DisplayName("Animation costs one mana, uses the stack, and works while tapped")
    void animationUsesStackAndPaysManaWhileTapped() {
        Permanent token = castAndGetToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(token.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.isLand(gd, token)).isTrue();
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animation expires during cleanup and the token retains its mana ability")
    void animationExpiresAtEndOfTurn() {
        Permanent token = castAndGetToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(gqs.isLand(gd, token)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.CHANGELING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);

        token.untap();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated animation leaves the token a 2/2 and does not untap it")
    void repeatedAnimationDoesNotIncreasePowerOrUntap() {
        Permanent token = castAndGetToken();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        harness.activateAbility(player1, tokenIndex, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, tokenIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.isLand(gd, token)).isTrue();
        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent castAndGetToken() {
        harness.castFromHand(player1, new MutableExplorer(), "{2}{G}");
        resolveAllTriggers();
        return findPermanent(player1, "Mutavault");
    }
}
