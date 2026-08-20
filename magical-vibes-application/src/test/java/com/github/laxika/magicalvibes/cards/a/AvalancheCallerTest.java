package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(snowLand.getCard().hasType(CardType.LAND)).isTrue();
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
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(forest);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addCallerReady(player1);
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bear);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCallerReady(Player player) {
        Permanent caller = new Permanent(new AvalancheCaller());
        caller.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(caller);
        return caller;
    }

    private Permanent addSnowLand(Player player) {
        Permanent snowLand = new Permanent(new Forest());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
        return snowLand;
    }
}
