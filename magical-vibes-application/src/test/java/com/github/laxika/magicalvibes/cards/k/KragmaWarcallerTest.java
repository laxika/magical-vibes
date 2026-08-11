package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FelhideMinotaur;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KragmaWarcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Minotaurs you control have haste")
    void grantsHasteToOwnMinotaurs() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent minotaur = addCreatureReady(player1, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Kragma Warcaller does not grant haste to non-Minotaurs or opposing Minotaurs")
    void hasteGrantIsLimitedToOwnMinotaurs() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingMinotaur = addCreatureReady(player2, new FelhideMinotaur());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingMinotaur, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A Minotaur that attacks gets +2/+0 until end of turn")
    void boostsAttackingMinotaur() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent minotaur = addCreatureReady(player1, new FelhideMinotaur());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(minotaur.getPowerModifier()).isEqualTo(2);
        assertThat(minotaur.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A non-Minotaur attacker does not get the attack bonus")
    void doesNotBoostNonMinotaurAttacker() {
        addCreatureReady(player1, new KragmaWarcaller());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(bear.getPowerModifier()).isZero();
    }
}
