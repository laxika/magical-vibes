package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CelestineCaveWitch;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HistoriansBoon.class, GloriousAnthem.class, HistoryOfBenalia.class, CelestineCaveWitch.class})
class HistoriansBoonTest extends BaseCardTest {

    @Test
    void createsSoldiersForItsOwnAndAnotherNontokenEnchantmentEntry() {
        harness.castFromHand(player1, new HistoriansBoon(), "{3}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    void doesNotTriggerForAnEnchantmentToken() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        harness.castFromHand(player1, new CelestineCaveWitch(), "{3}{B}");
        resolveAllTriggers();

        Permanent witch = findPermanent(player1, "Celestine Cave Witch");
        witch.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(witch)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent insect = findPermanents(player1, "Insect").get(0);
        harness.handlePermanentChosen(player1, insect.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Curse")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void createsAnAngelWhenAControlledSagaFinalChapterResolves() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void angelTriggerResolvesBeforeTheFinalChapterAbility() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isInstanceOf(HistoriansBoon.class);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getCard()).isInstanceOf(HistoryOfBenalia.class);
        resolveAllTriggers();
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    void doesNotCreateAnAngelForANonfinalChapter() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    void doesNotCreateAnAngelForAnOpponentsFinalChapter() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        Permanent saga = harness.addToBattlefieldAndReturn(player2, new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).isEmpty();
        assertThat(findPermanents(player2, "Angel")).isEmpty();
    }
}
