package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightAssassin.class, GiantSpider.class, GrizzlyBears.class})
class MidnightAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundBlocker() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent assassin = addCreatureReady(player1, new MidnightAssassin());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(assassin)));
        harness.beginBlockerDeclarationInput();

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
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(assassin)));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(assassin))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(assassin);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
