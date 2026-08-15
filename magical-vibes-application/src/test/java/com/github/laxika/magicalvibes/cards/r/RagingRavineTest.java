package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagingRavineTest extends BaseCardTest {

    @Test
    @DisplayName("Raging Ravine enters tapped and adds red or green mana")
    void entersTappedAndAddsChosenMana() {
        harness.setHand(player1, List.of(new RagingRavine()));
        harness.playLand(player1, 0);

        Permanent ravine = findPermanent(player1, "Raging Ravine");
        assertThat(ravine.isTapped()).isTrue();

        ravine.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Raging Ravine becomes a 3/3 red and green Elemental and stays a land")
    void animatesIntoRagingRavine() {
        Permanent ravine = addReadyRavine(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ravine)).isTrue();
        assertThat(gqs.isLand(gd, ravine)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ravine)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ravine)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, ravine))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(ravine.getTransientSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Raging Ravine gets a +1/+1 counter when it attacks")
    void getsCounterWhenItAttacks() {
        Permanent ravine = addReadyRavine(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ravine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Raging Ravine's animation and attack ability end at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent ravine = addReadyRavine(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ravine)).isFalse();
        assertThat(gqs.isLand(gd, ravine)).isTrue();
        assertThat(ravine.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
    }

    private void addAnimationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private Permanent addReadyRavine(Player player) {
        Permanent permanent = new Permanent(new RagingRavine());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
