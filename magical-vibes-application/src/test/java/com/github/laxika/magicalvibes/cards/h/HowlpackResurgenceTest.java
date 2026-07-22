package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HowlpackResurgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Wolves you control get +1/+1 and trample")
    void buffsWolvesYouControl() {
        harness.addToBattlefield(player1, new HowlpackResurgence());
        harness.addToBattlefield(player1, new WyluliWolf());

        Permanent wolf = findPermanent(player1, "Wyluli Wolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Werewolves you control get +1/+1 and trample")
    void buffsWerewolvesYouControl() {
        harness.addToBattlefield(player1, new HowlpackResurgence());
        harness.addToBattlefield(player1, new GreaterWerewolf());

        Permanent werewolf = findPermanent(player1, "Greater Werewolf");
        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, werewolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-Wolf/non-Werewolf creatures")
    void doesNotBuffOtherCreatures() {
        harness.addToBattlefield(player1, new HowlpackResurgence());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Wolves")
    void doesNotBuffOpponentWolves() {
        harness.addToBattlefield(player1, new HowlpackResurgence());
        harness.addToBattlefield(player2, new WyluliWolf());

        Permanent wolf = findPermanent(player2, "Wyluli Wolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new HowlpackResurgence());
        harness.addToBattlefield(player1, new WyluliWolf());

        Permanent wolf = findPermanent(player1, "Wyluli Wolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);

        wolf.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Can cast during opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new HowlpackResurgence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.getGameService().passPriority(gd, player2);
        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Howlpack Resurgence");
    }
}
