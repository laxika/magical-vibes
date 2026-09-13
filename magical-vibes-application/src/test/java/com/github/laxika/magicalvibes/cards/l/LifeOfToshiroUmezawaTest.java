package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MemoryOfToshiro;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeOfToshiroUmezawa.class, MemoryOfToshiro.class, GrizzlyBears.class})
class LifeOfToshiroUmezawaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I can give a creature +2/+2")
    void chapterIBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleListChoice(player1, "Target creature gets +2/+2 until end of turn.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter II can give a creature -1/-1")
    void chapterIINegativelyBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handleListChoice(player1, "Target creature gets -1/-1 until end of turn.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The life-gain mode needs no creature target")
    void chapterCanGainLifeWithoutCreatures() {
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleListChoice(player1, "You gain 2 life.");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Chapter III returns the Saga transformed under your control")
    void chapterIIITransformsIntoMemory() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent memory = findPermanent(player1, "Memory of Toshiro");
        assertThat(memory).isNotNull();
        assertThat(memory.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Memory of Toshiro pays one life for restricted black mana")
    void memoryPaysLifeForInstantSorceryMana() {
        LifeOfToshiroUmezawa front = new LifeOfToshiroUmezawa();
        Permanent memory = new Permanent(front);
        memory.setCard(front.getBackFaceCard());
        memory.setTransformed(true);
        memory.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(memory);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(memory.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(
                com.github.laxika.magicalvibes.model.ManaColor.BLACK)).isEqualTo(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new LifeOfToshiroUmezawa());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
