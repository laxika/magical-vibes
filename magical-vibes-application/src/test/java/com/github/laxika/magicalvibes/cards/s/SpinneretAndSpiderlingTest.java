package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollySpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinneretAndSpiderling.class, WoollySpider.class, GrizzlyBears.class, Forest.class})
class SpinneretAndSpiderlingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when at least two Spiders attack")
    void putsCounterWhenTwoSpidersAttack() {
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        addCreatureReady(player1, new WoollySpider());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(spinneret.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when fewer than two Spiders attack")
    void doesNotTriggerWithFewerThanTwoSpiders() {
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(spinneret.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exiles the top card after dealing at least four damage")
    void exilesTopCardAfterDealingAtLeastFourDamage() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        spinneret.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Does not trigger from dealing less than four damage")
    void doesNotTriggerFromLessThanFourDamage() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
