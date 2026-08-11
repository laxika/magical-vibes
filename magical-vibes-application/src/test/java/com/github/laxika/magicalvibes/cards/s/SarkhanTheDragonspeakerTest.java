package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
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

    private void advanceIntoDrawStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadySarkhan(Player player, int loyalty) {
        Permanent perm = new Permanent(new SarkhanTheDragonspeaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
