package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HighRiseSawjack;
import com.github.laxika.magicalvibes.cards.j.JewelThief;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightAssassin.class, HighRiseSawjack.class, JewelThief.class})
class MidnightAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundBlocker() {
        Permanent blocker = addCreatureReady(player2, new JewelThief());
        Permanent assassin = addCreatureReady(player1, new MidnightAssassin());

        declareAttackersAndPrepareBlockers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(assassin)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(assassin)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent assassin = addCreatureReady(player1, new MidnightAssassin());
        Permanent blocker = addCreatureReady(player2, new HighRiseSawjack());

        declareAttackersAndPrepareBlockers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(assassin)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(assassin))));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(assassin);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Deathtouch also destroys a larger creature when the Assassin blocks")
    void deathtouchDestroysLargerAttacker() {
        Permanent attacker = addCreatureReady(player1, new HighRiseSawjack());
        Permanent assassin = addCreatureReady(player2, new MidnightAssassin());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(assassin);
    }

    @Test
    @DisplayName("Deathtouch does not destroy a creature when the Assassin deals zero damage")
    void zeroDamageDoesNotDestroyAttacker() {
        Permanent attacker = addCreatureReady(player1, new HighRiseSawjack());
        Permanent assassin = addCreatureReady(player2, new MidnightAssassin());
        assassin.setPowerModifier(-1);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(assassin);
    }
}
