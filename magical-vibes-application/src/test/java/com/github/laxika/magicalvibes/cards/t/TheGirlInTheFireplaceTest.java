package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGirlInTheFireplace.class, GrizzlyBears.class, Shock.class, TheTenthDoctor.class})
class TheGirlInTheFireplaceTest extends BaseCardTest {

    @Test
    void chapterICreatesProtectedVanishingHumanNoble() {
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent human = findPermanent(player1, "Human Noble");
        assertThat(human.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(human.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.NOBLE);
        assertThat(human.getCard().getKeywords()).contains(Keyword.VANISHING);
        assertThat(human.getCounterCount(CounterType.TIME)).isEqualTo(3);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, human.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(human);
    }

    @Test
    void chapterIIVestsDoctorsWithHorsemanship() {
        addSagaWithLore(1);
        Permanent doctor = addCreatureReady(player1, new TheTenthDoctor());

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent horse = findPermanent(player1, "Horse");
        assertThat(horse.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(horse.getCard().getSubtypes()).contains(CardSubtype.HORSE);
        assertThat(gqs.hasKeyword(gd, doctor, Keyword.HORSEMANSHIP)).isTrue();
    }

    @Test
    void chapterIIITimeTravelsWhenAnAllyDealsCombatDamage() {
        addSagaWithLore(2);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent timeTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        timeTarget.setCounterCount(CounterType.TIME, 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.handleListChoice(player1, "ADD");

        assertThat(timeTarget.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new TheGirlInTheFireplace());
        Permanent saga = findPermanent(player1, "The Girl in the Fireplace");
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
