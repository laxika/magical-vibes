package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlaughterSinger.class, CrawlingChorus.class, GrizzlyBears.class})
class SlaughterSingerTest extends BaseCardTest {

    @Test
    @DisplayName("Another toxic creature gets +1/+1 when it attacks")
    void boostsAnotherToxicAttacker() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent toxicCreature = addCreatureReady(player1, new CrawlingChorus());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-toxic creature does not trigger the ability")
    void doesNotBoostNonToxicAttacker() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent nonToxicCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nonToxicCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonToxicCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slaughter Singer does not trigger for itself")
    void doesNotBoostItselfWhenItAttacks() {
        Permanent singer = addCreatureReady(player1, new SlaughterSinger());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent toxicCreature = addCreatureReady(player1, new CrawlingChorus());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, toxicCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, toxicCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Toxic 2 gives two poison counters with combat damage without using the stack")
    void toxicAppliesWithCombatDamage() {
        Permanent singer = addCreatureReady(player1, new SlaughterSinger());
        singer.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each toxic attacker gets a bonus, while a nonattacking creature does not")
    void boostsEachToxicAttacker() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent first = addCreatureReady(player1, new CrawlingChorus());
        Permanent second = addCreatureReady(player1, new CrawlingChorus());
        Permanent nonattacker = addCreatureReady(player1, new CrawlingChorus());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonattacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Slaughter Singers boost each other and both boost another toxic attacker")
    void multipleSingersStackTheirBonuses() {
        Permanent first = addCreatureReady(player1, new SlaughterSinger());
        Permanent second = addCreatureReady(player1, new SlaughterSinger());
        Permanent chorus = addCreatureReady(player1, new CrawlingChorus());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, chorus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chorus)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's toxic attacker does not get a bonus")
    void doesNotBoostOpponentsAttacker() {
        addCreatureReady(player1, new SlaughterSinger());
        Permanent chorus = addCreatureReady(player2, new CrawlingChorus());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, chorus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chorus)).isEqualTo(1);
    }
}
