package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredScrapgorger;
import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkrelvsHive.class, CrawlingChorus.class, ArmoredScrapgorger.class})
class SkrelvsHiveTest extends BaseCardTest {

    @Test
    @DisplayName("At its controller's upkeep, loses 1 life and creates a Mite that can't block")
    void losesLifeAndCreatesMiteAtUpkeep() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        Permanent mite = findPermanent(player1, "Mite");

        assertThat(mite.getCard().isToken()).isTrue();
        assertThat(mite.getCard().getName()).isEqualTo("Mite");
        assertThat(mite.getCard().getPower()).isEqualTo(1);
        assertThat(mite.getCard().getToughness()).isEqualTo(1);
        assertThat(mite.getCard().getSubtypes()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardSubtype.PHYREXIAN,
                com.github.laxika.magicalvibes.model.CardSubtype.MITE);
        assertThat(mite.hasKeyword(Keyword.TOXIC)).isTrue();
        assertThat(mite.getCard().getColor()).isNull();
        assertThat(mite.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(mite.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(mite.isTapped()).isFalse();
        assertThat(bls.canBlock(gd, mite)).isFalse();
    }

    @Test
    @DisplayName("Toxic creatures you control gain lifelink when an opponent has three poison counters")
    void grantsLifelinkToToxicCreaturesAtCorruptedThreshold() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent mite = findPermanent(player1, "Mite");

        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isFalse();
        gd.playerPoisonCounters.put(player2.getId(), 2);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isFalse();
        gd.playerPoisonCounters.put(player2.getId(), 3);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isTrue();
        gd.playerPoisonCounters.put(player2.getId(), 2);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken())).isTrue();
    }

    @Test
    void miteToxicAppliesWithCombatDamageWithoutUsingStack() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent mite = findPermanent(player1, "Mite");
        mite.setSummoningSick(false);
        mite.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void corruptedLifelinkGainsLifeFromMiteCombatDamage() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.playerPoisonCounters.put(player2.getId(), 3);
        Permanent mite = findPermanent(player1, "Mite");
        mite.setSummoningSick(false);
        mite.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void corruptedOnlyGrantsLifelinkToOwnToxicCreatures() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        Permanent ownToxic = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        Permanent opposingToxic = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        Permanent nonToxic = harness.addToBattlefieldAndReturn(player1, new ArmoredScrapgorger());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThat(gqs.hasKeyword(gd, ownToxic, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingToxic, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonToxic, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void controllersPoisonDoesNotEnableCorrupted() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void reachingCorruptedDuringDamageDoesNotRetroactivelyGainLife() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.playerPoisonCounters.put(player2.getId(), 2);
        Permanent mite = findPermanent(player1, "Mite");
        mite.setSummoningSick(false);
        mite.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 19);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isTrue();
    }
}
