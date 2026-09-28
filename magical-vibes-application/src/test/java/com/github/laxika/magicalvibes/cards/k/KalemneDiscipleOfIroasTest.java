package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalemneDiscipleOfIroas.class, CrawWurm.class, GrizzlyBears.class, Shock.class})
class KalemneDiscipleOfIroasTest extends BaseCardTest {

    @Test
    void gainsExperienceForCreatureSpellsWithManaValueFiveOrGreater() {
        harness.addToBattlefield(player1, new KalemneDiscipleOfIroas());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void doesNotGainExperienceForSmallerOrNoncreatureSpells() {
        harness.addToBattlefield(player1, new KalemneDiscipleOfIroas());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void getsPlusOnePlusOneForEachExperienceCounter() {
        Permanent kalemne = harness.addToBattlefieldAndReturn(player1, new KalemneDiscipleOfIroas());
        gd.playerExperienceCounters.put(player1.getId(), 3);

        assertThat(gqs.getEffectivePower(gd, kalemne)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, kalemne)).isEqualTo(6);

        gd.playerExperienceCounters.put(player1.getId(), 1);

        assertThat(gqs.getEffectivePower(gd, kalemne)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kalemne)).isEqualTo(4);
    }
}
