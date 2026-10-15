package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Tidings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalemneDiscipleOfIroas.class, CrawWurm.class, GrizzlyBears.class, Shock.class,
        AirElemental.class, Tidings.class})
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

    @Test
    void gainsExperienceAtExactlyFiveManaValueBeforeCreatureResolves() {
        Permanent kalemne = harness.addToBattlefieldAndReturn(player1, new KalemneDiscipleOfIroas());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        assertThat(gqs.getEffectivePower(gd, kalemne)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kalemne)).isEqualTo(4);
        resolveAllTriggers();
    }

    @Test
    void opponentsCreatureSpellDoesNotGrantExperience() {
        harness.addToBattlefield(player1, new KalemneDiscipleOfIroas());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CrawWurm()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player2.getId());
    }

    @Test
    void enteringWithoutBeingCastDoesNotGrantExperience() {
        harness.addToBattlefield(player1, new KalemneDiscipleOfIroas());

        harness.enterBattlefieldAndReturn(player1, new CrawWurm());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void noncreatureSpellWithManaValueFiveDoesNotGrantExperience() {
        harness.addToBattlefield(player1, new KalemneDiscipleOfIroas());
        harness.setHand(player1, List.of(new Tidings()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void experienceTriggerResolvesAfterKalemneLeavesAndCountersBoostNewKalemne() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KalemneDiscipleOfIroas());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new KalemneDiscipleOfIroas());
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(4);
    }

    @Test
    void boostUsesCurrentControllersExperienceAndDoesNotBoostOtherCreatures() {
        Permanent kalemne = harness.addToBattlefieldAndReturn(player1, new KalemneDiscipleOfIroas());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 2);
        gd.playerExperienceCounters.put(player2.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, kalemne)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(kalemne);
        gd.playerBattlefields.get(player2.getId()).add(kalemne);

        assertThat(gqs.getEffectivePower(gd, kalemne)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, kalemne)).isEqualTo(7);
    }
}
