package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.a.ArrowStorm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({SarkhanTheDragonspeaker.class, AlpineGrizzly.class, ArrowStorm.class, Forest.class, Mountain.class})
class SarkhanTheDragonspeakerTest extends BaseCardTest {

    @Test
    @DisplayName("+1 makes Sarkhan a red 4/4 Dragon creature without the planeswalker type")
    void plusOneAnimatesSarkhan() {
        Permanent sarkhan = addReadySarkhan(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.isCreature(gd, sarkhan)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, sarkhan)).containsExactly(CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, sarkhan)).contains(com.github.laxika.magicalvibes.model.CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("-3 deals 4 damage to a target creature")
    void minusThreeDealsDamageToCreature() {
        Permanent sarkhan = addReadySarkhan(player1, 4);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Alpine Grizzly");
    }

    @Test
    @DisplayName("The emblem draws two additional cards at the controller's draw step")
    void emblemDrawsAdditionalCards() {
        Permanent sarkhan = addReadySarkhan(player1, 6);
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Forest(), new Mountain()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        gd.turnNumber = 2;
        advanceIntoDrawStep(player1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("The emblem discards the controller's hand at the end step")
    void emblemDiscardsHand() {
        Permanent sarkhan = addReadySarkhan(player1, 6);
        harness.setHand(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceIntoEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void animatedSarkhanSurvivesLethalDamageWithoutLosingLoyalty() {
        Permanent sarkhan = addReadySarkhan(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ArrowStorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, sarkhan.getId());

        harness.assertOnBattlefield(player1, "Sarkhan, the Dragonspeaker");
        assertThat(sarkhan.getMarkedDamage()).isEqualTo(4);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Sarkhan, the Dragonspeaker");
        assertThat(gqs.isCreature(gd, sarkhan)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, sarkhan)).isTrue();
        assertThat(sarkhan.getMarkedDamage()).isZero();
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sarkhan, Keyword.HASTE)).isFalse();
    }

    @Test
    void minusThreeResolvesAfterSarkhanDiesPayingItsCost() {
        addReadySarkhan(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.assertInGraveyard(player1, "Sarkhan, the Dragonspeaker");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpine Grizzly");
    }

    @Test
    void emblemDoesNotTriggerDuringOpponentsSteps() {
        addReadySarkhan(player1, 6);
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Forest(), new Mountain(), new Forest(), new Mountain()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gd.turnNumber = 2;

        advanceIntoDrawStep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void emblemDiscardWaitsForResolutionAndLeavesOpponentsHandAlone() {
        addReadySarkhan(player1, 6);
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player2, List.of(new Forest()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sarkhan, the Dragonspeaker");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void advanceIntoDrawStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SarkhanTheDragonspeaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
