package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.cards.t.TokTokVolcanoBorn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiLavarunner.class, TokTokVolcanoBorn.class, GrizzlyBears.class, SoulsFire.class})
class AkkiLavarunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Flips after dealing combat damage to the defending player")
    void flipsAfterCombatDamageToOpponent() {
        Permanent akki = addCreatureReady(player1, new AkkiLavarunner());

        declareAttackers(List.of(0));
        resolveCombat(player1);
        resolveAllTriggers(); // the damage trigger resolves

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(akki.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip when its combat damage only hits a blocker")
    void staysUnflippedWhenBlocked() {
        Permanent akki = addCreatureReady(player1, new AkkiLavarunner());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(akki.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips after dealing noncombat damage to the opponent")
    void flipsAfterNoncombatDamageToOpponent() {
        Permanent akki = addCreatureReady(player1, new AkkiLavarunner());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(akki.getId(), player2.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(akki.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip when its noncombat damage hits its controller")
    void staysUnflippedWhenNoncombatDamageHitsController() {
        Permanent akki = addCreatureReady(player1, new AkkiLavarunner());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(akki.getId(), player1.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(akki.isTransformed()).isFalse();
    }
}
