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
    void enteringSagaTriggersFirstChapterAndHumanEntersWithThreeTimeCounters() {
        harness.setHand(player1, List.of(new TheGirlInTheFireplace()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Noble")).isEqualTo(1);
        assertThat(findPermanent(player1, "Human Noble").getCounterCount(CounterType.TIME))
                .isEqualTo(3);
        assertThat(findPermanent(player1, "The Girl in the Fireplace").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

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
        harness.castAndResolveInstant(player2, 0, human.getId());
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
        resolveAllTriggers();
        harness.handleListChoice(player1, "ADD");

        assertThat(timeTarget.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void vanishingRemovesCountersOnOwnUpkeepAndSacrificesAfterTheLast() {
        addSagaWithLore(0);
        advanceToNextChapter();
        resolveAllTriggers();
        Permanent human = findPermanent(player1, "Human Noble");

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(human.getCounterCount(CounterType.TIME)).isEqualTo(3);

        for (int remaining = 2; remaining >= 0; remaining--) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
            assertThat(human.getCounterCount(CounterType.TIME)).isEqualTo(remaining);
            assertThat(gd.playerBattlefields.get(player1.getId()).contains(human))
                    .isEqualTo(remaining > 0);
        }
    }

    @Test
    void removingLastTimeCounterThroughTimeTravelSacrificesHuman() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        resolveAllTriggers();
        Permanent human = findPermanent(player1, "Human Noble");
        human.setCounterCount(CounterType.TIME, 1);
        human.setSummoningSick(false);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "REMOVE");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(human);
    }

    @Test
    void horseOnlyGrantsHorsemanshipToItsControllersDoctorsWhilePresent() {
        addSagaWithLore(1);
        Permanent ownDoctor = addCreatureReady(player1, new TheTenthDoctor());
        Permanent opposingDoctor = addCreatureReady(player2, new TheTenthDoctor());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        resolveAllTriggers();
        Permanent horse = findPermanent(player1, "Horse");

        assertThat(gqs.hasKeyword(gd, ownDoctor, Keyword.HORSEMANSHIP)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingDoctor, Keyword.HORSEMANSHIP)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HORSEMANSHIP)).isFalse();
        assertThat(gqs.hasKeyword(gd, horse, Keyword.HORSEMANSHIP)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, horse.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(horse);
        assertThat(gqs.hasKeyword(gd, ownDoctor, Keyword.HORSEMANSHIP)).isFalse();
    }

    @Test
    void chapterIIITimeTravelsOnceForEachCreatureDealingCombatDamage() {
        addSagaWithLore(2);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.TIME, 2);

        advanceToNextChapter();
        resolveAllTriggers();
        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "SKIP");
        resolveAllTriggers();
        harness.handleListChoice(player1, "REMOVE");
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.assertLife(player2, 16);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheGirlInTheFireplace());
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
