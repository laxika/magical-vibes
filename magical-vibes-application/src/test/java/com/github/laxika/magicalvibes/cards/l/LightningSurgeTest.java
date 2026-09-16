package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningSurge.class, GiantWarthog.class, HealingSalve.class})
class LightningSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals four damage below threshold")
    void dealsFourDamageBelowThreshold() {
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals six damage and cannot be prevented at threshold")
    void dealsSixDamageAndCannotBePreventedAtThreshold() {
        harness.setGraveyard(player1, filler(7));
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Threshold damage to a creature cannot be prevented")
    void thresholdDamageToCreatureCannotBePrevented() {
        harness.addToBattlefield(player2, new GiantWarthog());
        harness.setGraveyard(player1, filler(7));
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Giant Warthog");
        harness.castSorcery(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Warthog");
        harness.assertInGraveyard(player2, "Giant Warthog");
    }

    @Test
    @DisplayName("Flashback deals threshold damage and exiles Lightning Surge")
    void flashbackDealsThresholdDamageAndExilesCard() {
        LightningSurge surge = new LightningSurge();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(surge);
        graveyard.addAll(filler(7));
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        harness.assertNotInGraveyard(player1, "Lightning Surge");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lightning Surge"));
    }

    @Test
    @DisplayName("Flashback below threshold counts only cards remaining in the graveyard")
    void flashbackBelowThresholdAfterLightningSurgeLeavesGraveyard() {
        LightningSurge surge = new LightningSurge();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(surge);
        graveyard.addAll(filler(6));
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GiantWarthog());
        }
        return cards;
    }
}
