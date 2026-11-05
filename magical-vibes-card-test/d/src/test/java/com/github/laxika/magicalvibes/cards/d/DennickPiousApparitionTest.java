package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DennickPiousApparition.class, GrizzlyBears.class, Shock.class})
class DennickPiousApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card of yours put into a graveyard investigates")
    void ownCreatureCardInvestigates() {
        harness.addToBattlefield(player1, new DennickPiousApparition());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        shock(bears);

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature card put into a graveyard investigates")
    void opponentCreatureCardInvestigates() {
        harness.addToBattlefield(player1, new DennickPiousApparition());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        shock(bears);

        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability triggers only once each turn, whoever owns the creature cards")
    void triggersOnlyOnceEachTurnAcrossBothOwners() {
        harness.addToBattlefield(player1, new DennickPiousApparition());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        shock(ownBears);
        shock(opponentBears);

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
    }

    private void shock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
