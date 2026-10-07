package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulbrightFlamekin.class, Forest.class, Tarfire.class})
class SoulbrightFlamekinTest extends BaseCardTest {

    @Test
    @DisplayName("Each activation grants trample to the target; no mana burst before the third")
    void grantsTrampleNoManaBeforeThird() {
        addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent bears = addCreatureReady(player1, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 4);

        activateTargeting(bears);
        activateTargeting(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Third resolution adds eight red mana")
    void thirdResolutionAddsEightRed() {
        addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent bears = addCreatureReady(player1, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 6);

        activateTargeting(bears);
        activateTargeting(bears);
        activateTargeting(bears);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(8);
    }

    @Test
    @DisplayName("Targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent forest = addCreatureReady(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateTargeting(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The controller can decline the third resolution's mana without losing trample")
    void canDeclineMana() {
        Permanent flamekin = addCreatureReady(player1, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 6);

        activateTargeting(flamekin);
        activateTargeting(flamekin);
        activateTargeting(flamekin);

        assertThat(gqs.hasKeyword(gd, flamekin, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Fourth resolution grants trample but offers no additional mana")
    void fourthResolutionDoesNotProduceMana() {
        Permanent flamekin = addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent opponent = addCreatureReady(player2, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 6);

        activateTargeting(flamekin);
        activateTargeting(flamekin);
        activateTargeting(flamekin);
        harness.handleMayAbilityChosen(player1, true);
        harness.ensurePriority(player1);
        activateTargeting(opponent);

        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(6);
    }

    @Test
    @DisplayName("Different Flamekins do not share their resolution count")
    void resolutionsAreCountedPerSource() {
        Permanent first = addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent second = addCreatureReady(player1, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 6);

        activateTargeting(first);
        activateTargeting(first);
        harness.activateAbility(player1, 1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Trample expires and the resolution count resets on the next turn")
    void effectsResetNextTurn() {
        Permanent flamekin = addCreatureReady(player1, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 4);
        activateTargeting(flamekin);
        activateTargeting(flamekin);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, flamekin, Keyword.TRAMPLE)).isFalse();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.ensurePriority(player1);
        activateTargeting(flamekin);

        assertThat(gqs.hasKeyword(gd, flamekin, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("An ability whose target dies does not count as a resolution")
    void illegalTargetDoesNotAdvanceCount() {
        Permanent source = addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent target = addCreatureReady(player2, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Tarfire()));

        activateTargeting(source);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.ensurePriority(player1);
        activateTargeting(source);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The third resolution can produce mana after its source leaves the battlefield")
    void sourceLeavingDoesNotPreventMana() {
        Permanent source = addCreatureReady(player1, new SoulbrightFlamekin());
        Permanent target = addCreatureReady(player2, new SoulbrightFlamekin());
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Tarfire()));

        activateTargeting(target);
        activateTargeting(target);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(8);
    }
}
