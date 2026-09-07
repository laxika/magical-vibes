package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuqAtaLancer.class, Python.class})
class SuqAtaLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingHitsNonFlankingBlocker() {
        Permanent lancer = addCreatureReady(player1, new SuqAtaLancer());
        lancer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Python());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker that has flanking")
    void flankingDoesNotAffectFlankingBlocker() {
        Permanent lancer = addCreatureReady(player1, new SuqAtaLancer());
        lancer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SuqAtaLancer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Haste lets Suq'Ata Lancer attack the turn it enters")
    void hasteLetsItAttackWhileSummoningSick() {
        harness.setLife(player2, 20);

        Permanent lancer = new Permanent(new SuqAtaLancer());
        gd.playerBattlefields.get(player1.getId()).add(lancer);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
