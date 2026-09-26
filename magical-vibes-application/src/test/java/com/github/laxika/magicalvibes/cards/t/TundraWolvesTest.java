package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PearledUnicorn;
import com.github.laxika.magicalvibes.cards.s.ScrybSprites;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TundraWolves.class, ScrybSprites.class, PearledUnicorn.class})
class TundraWolvesTest extends BaseCardTest {

    @Test
    @DisplayName("First-strike damage kills a 1/1 before it can deal regular combat damage")
    void firstStrikeDamageResolvesBeforeRegularCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new TundraWolves());
        Permanent blocker = addCreatureReady(player2, new ScrybSprites());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).doesNotContain(blocker.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("First strike does not prevent regular combat damage")
    void firstStrikeCreatureStillTakesRegularCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new TundraWolves());
        Permanent blocker = addCreatureReady(player2, new PearledUnicorn());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).doesNotContain(attacker.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).contains(blocker.getId());
    }
}
