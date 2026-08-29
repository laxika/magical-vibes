package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.cards.i.IcatianStore;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandOfJustice.class, IcatianInfantry.class, RiverMerfolk.class, IcatianStore.class})
class HandOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three white creatures destroys the target creature")
    void tapsThreeWhiteCreaturesAndDestroysTarget() {
        Permanent hand = addCreatureReady(player1, new HandOfJustice());
        Permanent whiteCreature1 = addCreatureReady(player1, new IcatianInfantry());
        Permanent whiteCreature2 = addCreatureReady(player1, new IcatianInfantry());
        Permanent whiteCreature3 = addCreatureReady(player1, new IcatianInfantry());
        Permanent whiteCreature4 = addCreatureReady(player1, new IcatianInfantry());
        Permanent target = addCreatureReady(player2, new RiverMerfolk());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hand), null, target.getId());
        harness.handlePermanentChosen(player1, whiteCreature1.getId());
        harness.handlePermanentChosen(player1, whiteCreature2.getId());
        harness.handlePermanentChosen(player1, whiteCreature3.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(whiteCreature1.isTapped()).isTrue();
        assertThat(whiteCreature2.isTapped()).isTrue();
        assertThat(whiteCreature3.isTapped()).isTrue();
        assertThat(whiteCreature4.isTapped()).isFalse();
        assertThat(hand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without three untapped white creatures")
    void cannotActivateWithoutThreeUntappedWhiteCreatures() {
        Permanent hand = addCreatureReady(player1, new HandOfJustice());
        addCreatureReady(player1, new IcatianInfantry());
        addCreatureReady(player1, new IcatianInfantry());
        Permanent target = addCreatureReady(player2, new RiverMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hand), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use a nonwhite creature to pay the cost")
    void cannotUseNonwhiteCreatureToPayCost() {
        Permanent hand = addCreatureReady(player1, new HandOfJustice());
        addCreatureReady(player1, new IcatianInfantry());
        addCreatureReady(player1, new IcatianInfantry());
        addCreatureReady(player1, new RiverMerfolk());
        Permanent target = addCreatureReady(player2, new RiverMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hand), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use a tapped white creature to pay the cost")
    void cannotUseTappedWhiteCreatureToPayCost() {
        Permanent hand = addCreatureReady(player1, new HandOfJustice());
        Permanent whiteCreature1 = addCreatureReady(player1, new IcatianInfantry());
        Permanent whiteCreature2 = addCreatureReady(player1, new IcatianInfantry());
        Permanent tappedWhiteCreature = addCreatureReady(player1, new IcatianInfantry());
        tappedWhiteCreature.tap();
        Permanent target = addCreatureReady(player2, new RiverMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hand), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hand.isTapped()).isFalse();
        assertThat(whiteCreature1.isTapped()).isFalse();
        assertThat(whiteCreature2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent hand = addCreatureReady(player1, new HandOfJustice());
        addCreatureReady(player1, new IcatianInfantry());
        addCreatureReady(player1, new IcatianInfantry());
        addCreatureReady(player1, new IcatianInfantry());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2,
                new IcatianStore());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hand), null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
