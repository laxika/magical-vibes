package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.q.QuirionExplorer;
import com.github.laxika.magicalvibes.cards.r.RadiantKavu;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggJailer.class, QuirionExplorer.class, RadiantKavu.class, TerminalMoraine.class})
class MoggJailerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when defending player controls an untapped creature with power 2 or less")
    void cannotAttackWhenDefenderControlsUntappedSmallCreature() {
        addCreatureReady(player1, new MoggJailer());
        addCreatureReady(player2, new QuirionExplorer());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when the qualifying defending creature is tapped")
    void canAttackWhenQualifyingCreatureIsTapped() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoggJailer());
        Permanent defendingCreature = addCreatureReady(player2, new QuirionExplorer());
        defendingCreature.tap();

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Can attack when the untapped defending creature has power greater than 2")
    void canAttackWhenDefendingCreatureIsLarger() {
        addCreatureReady(player1, new MoggJailer());
        addCreatureReady(player2, new RadiantKavu());

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Can attack when the opponent controls only an untapped noncreature permanent")
    void canAttackWhenDefenderControlsOnlyNoncreaturePermanent() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoggJailer());
        harness.addToBattlefield(player2, new TerminalMoraine());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Can attack when only its controller controls an untapped small creature")
    void canAttackWhenOnlyControllerControlsUntappedSmallCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoggJailer());
        addCreatureReady(player1, new QuirionExplorer());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }
}
