package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.cards.p.PhantomFlock;
import com.github.laxika.magicalvibes.cards.p.PrismaticStrands;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningSurge.class, MentalNote.class, PrismaticStrands.class, PhantomFlock.class})
class LightningSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals four damage below threshold")
    void dealsFourDamageBelowThreshold() {
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals six damage and cannot be prevented at threshold")
    void dealsSixDamageAndCannotBePreventedAtThreshold() {
        harness.setGraveyard(player1, filler(7));
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new PrismaticStrands()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, "RED");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
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

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        harness.assertNotInGraveyard(player1, "Lightning Surge");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Lightning Surge"));
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, filler(7));
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Threshold is checked when Lightning Surge resolves")
    void thresholdIsCheckedAtResolution() {
        harness.setGraveyard(player1, filler(6));
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.setGraveyard(player1, filler(7));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Threshold damage cannot be prevented when targeting a permanent")
    void thresholdDamageCannotBePreventedToPermanent() {
        harness.setGraveyard(player1, filler(7));
        Permanent flock = harness.addToBattlefieldAndReturn(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, flock.getId());

        harness.assertNotOnBattlefield(player2, "Phantom Flock");
        harness.assertInGraveyard(player2, "Phantom Flock");
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new MentalNote());
        }
        return cards;
    }
}
