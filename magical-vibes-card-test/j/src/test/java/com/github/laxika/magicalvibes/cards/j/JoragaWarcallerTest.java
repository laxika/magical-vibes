package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.s.Smother;
import com.github.laxika.magicalvibes.cards.s.SnappingCreeper;
import com.github.laxika.magicalvibes.cards.s.StrengthOfTheTajuru;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoragaWarcaller.class, ArborElf.class, SnappingCreeper.class,
        StrengthOfTheTajuru.class, Smother.class})
class JoragaWarcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters when not multikicked")
    void entersWithoutCountersWhenNotMultikicked() {
        harness.setHand(player1, List.of(new JoragaWarcaller()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent warcaller = findPermanent(player1, "Joraga Warcaller");
        assertThat(warcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets counters for multikicker payments and scales its Elf lord ability")
    void scalesOtherOwnElvesWithMultikickerCounters() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        Permanent ownNonElf = harness.addToBattlefieldAndReturn(player1, new SnappingCreeper());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new ArborElf());
        harness.setHand(player1, List.of(new JoragaWarcaller()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{G}", "{1}{G}"));
        harness.passBothPriorities();

        Permanent warcaller = findPermanent(player1, "Joraga Warcaller");
        assertThat(warcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters from another spell boost other Elves even without kicking")
    void countersAddedLaterScaleTheBoost() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new ArborElf());
        harness.setHand(player1, List.of(new JoragaWarcaller()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent warcaller = findPermanent(player1, "Joraga Warcaller");
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(1);

        harness.setHand(player1, List.of(new StrengthOfTheTajuru()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstantForX(player1, 0, 2, List.of(warcaller.getId()));
        harness.passBothPriorities();

        assertThat(warcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, warcaller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warcaller)).isEqualTo(3);

        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, warcaller.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(warcaller);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Warcallers boost each other without counting received boosts as counters")
    void multipleWarcallersUseOnlyTheirOwnCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JoragaWarcaller());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new JoragaWarcaller());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ArborElf());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);

        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }
}
