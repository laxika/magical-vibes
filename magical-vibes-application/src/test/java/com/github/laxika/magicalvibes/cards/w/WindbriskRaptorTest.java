package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.o.OldGhastbark;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindbriskRaptor.class, OldGhastbark.class})
class WindbriskRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creature you control gains its controller life via granted lifelink")
    void grantsLifelinkToOwnAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new WindbriskRaptor());
        addCreatureReady(player1, new OldGhastbark());

        declareAttackers(List.of(1));

        // Old Ghastbark (no innate lifelink) deals 3 combat damage; lifelink gains player1 3 life.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Without the Raptor, the same attacker gains no life (lifelink comes from the Raptor)")
    void noLifelinkWithoutRaptor() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new OldGhastbark());

        declareAttackers(List.of(0));

        // player2 loses 3; player1 gains nothing since Old Ghastbark has no lifelink.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Raptor itself gains lifelink while attacking")
    void grantsLifelinkToRaptorItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new WindbriskRaptor());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("The Raptor does not grant lifelink to an opponent's attacking creature")
    void doesNotGrantLifelinkToOpponentsAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new WindbriskRaptor());
        addCreatureReady(player2, new OldGhastbark());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
