package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent mite = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(mite.getCard().getName()).isEqualTo("Mite");
        assertThat(mite.getCard().getPower()).isEqualTo(1);
        assertThat(mite.getCard().getToughness()).isEqualTo(1);
        assertThat(mite.getCard().getSubtypes()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardSubtype.PHYREXIAN,
                com.github.laxika.magicalvibes.model.CardSubtype.MITE);
        assertThat(mite.hasKeyword(Keyword.TOXIC)).isTrue();
        assertThat(bls.canBlock(gd, mite)).isFalse();
    }

    @Test
    @DisplayName("Toxic creatures you control gain lifelink when an opponent has three poison counters")
    void grantsLifelinkToToxicCreaturesAtCorruptedThreshold() {
        harness.addToBattlefield(player1, new SkrelvsHive());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent mite = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isFalse();
        gd.playerPoisonCounters.put(player2.getId(), 2);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isFalse();
        gd.playerPoisonCounters.put(player2.getId(), 3);
        assertThat(gqs.hasKeyword(gd, mite, Keyword.LIFELINK)).isTrue();
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
}
