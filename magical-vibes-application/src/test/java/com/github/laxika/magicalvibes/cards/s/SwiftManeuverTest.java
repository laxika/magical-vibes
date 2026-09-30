package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftManeuver.class, KjeldoranOutrider.class})
class SwiftManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage to a target creature")
    void preventsDamageToTargetCreature() {
        Permanent defender = addCreatureReady(player2, new KjeldoranOutrider());
        castSwiftManeuver(defender.getId());

        Permanent attacker = addCreatureReady(player1, new KjeldoranOutrider());
        attacker.setAttacking(true);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        resolveCombat();

        assertThat(findPermanent(player2, "Kjeldoran Outrider").getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Kjeldoran Outrider");
    }

    @Test
    @DisplayName("Prevents the next 2 damage to a target player")
    void preventsDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        castSwiftManeuver(player2.getId());

        Permanent attacker = addCreatureReady(player1, new KjeldoranOutrider());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents only the next 2 damage to a target player")
    void preventsOnlyNextTwoDamage() {
        harness.setLife(player2, 20);
        castSwiftManeuver(player2.getId());

        addCreatureReady(player1, new KjeldoranOutrider());
        addCreatureReady(player1, new KjeldoranOutrider());

        declareAttackers(List.of(0, 1));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsAtNextUpkeep() {
        harness.setLibrary(player1, List.of(new KjeldoranOutrider()));
        castSwiftManeuver(player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    private void castSwiftManeuver(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SwiftManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
