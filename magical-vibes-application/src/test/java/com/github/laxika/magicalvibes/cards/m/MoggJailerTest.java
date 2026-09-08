package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoggJailerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when defending player controls an untapped creature with power 2 or less")
    void cannotAttackWhenDefenderControlsUntappedSmallCreature() {
        addCreatureReady(player1, new MoggJailer());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when the qualifying defending creature is tapped")
    void canAttackWhenQualifyingCreatureIsTapped() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoggJailer());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        defendingCreature.tap();

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Can attack when the untapped defending creature has power greater than 2")
    void canAttackWhenDefendingCreatureIsLarger() {
        addCreatureReady(player1, new MoggJailer());
        addCreatureReady(player2, new CrawWurm());

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }
}
