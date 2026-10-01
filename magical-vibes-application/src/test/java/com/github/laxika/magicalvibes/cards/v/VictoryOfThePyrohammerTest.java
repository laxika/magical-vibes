package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictoryOfThePyrohammer.class, ColossalDreadmaw.class, GrizzlyBears.class})
class VictoryOfThePyrohammerTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I deals 4 damage to creatures and planeswalkers and preserves creature damage")
    void chapterIDamagesCreaturesAndPlaneswalkersAndPreservesDamage() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent planeswalker = addTestPlaneswalker(player2);

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(saga).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Chapter II deals 1 damage to each creature and planeswalker")
    void chapterIIDealsOneDamage() {
        addSagaWithLore(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = addTestPlaneswalker(player2);

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III deals 1 damage before the Saga is sacrificed")
    void chapterIIIDealsOneDamageBeforeSacrifice() {
        addSagaWithLore(2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Victory of the Pyrohammer"));
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new VictoryOfThePyrohammer());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addTestPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{3}");
        card.setLoyalty(5);
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, 5);
        return permanent;
    }
}
