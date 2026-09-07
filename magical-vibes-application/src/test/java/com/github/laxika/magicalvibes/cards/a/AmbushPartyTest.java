package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbushParty.class, AysenBureaucrats.class})
class AmbushPartyTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Ambush Party attack the turn it enters")
    void hasteLetsItAttackImmediately() {
        harness.castFromHand(player1, new AmbushParty(), "{4}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("First strike destroys a blocker before regular combat damage")
    void firstStrikeDestroysBlockerBeforeRegularDamage() {
        addCreatureReady(player1, new AmbushParty());
        addCreatureReady(player2, new AysenBureaucrats());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ambush Party");
        harness.assertInGraveyard(player2, "Aysen Bureaucrats");
    }
}
