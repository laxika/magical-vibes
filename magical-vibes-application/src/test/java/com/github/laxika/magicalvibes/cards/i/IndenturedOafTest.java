package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DimirGuildmage;
import com.github.laxika.magicalvibes.cards.v.ViashinoSlasher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndenturedOaf.class, ViashinoSlasher.class, DimirGuildmage.class})
class IndenturedOafTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents its combat damage to red creatures")
    void preventsCombatDamageToRedCreature() {
        Permanent oaf = addCreatureReady(player1, new IndenturedOaf());
        Permanent redCreature = addCreatureReady(player2, new ViashinoSlasher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(redCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(redCreature);
        assertThat(oaf.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals combat damage to non-red creatures")
    void dealsCombatDamageToNonRedCreature() {
        Permanent oaf = addCreatureReady(player1, new IndenturedOaf());
        Permanent nonRedCreature = addCreatureReady(player2, new DimirGuildmage());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonRedCreature);
        harness.assertInGraveyard(player2, "Dimir Guildmage");
        assertThat(oaf.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals combat damage to players")
    void dealsCombatDamageToPlayer() {
        Permanent oaf = addCreatureReady(player1, new IndenturedOaf());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
        assertThat(oaf.getMarkedDamage()).isZero();
    }
}
