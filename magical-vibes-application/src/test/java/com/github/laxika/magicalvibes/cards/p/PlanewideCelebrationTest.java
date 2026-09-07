package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanewideCelebration.class, GrizzlyBears.class, Shock.class, Plains.class})
class PlanewideCelebrationTest extends BaseCardTest {

    @Test
    void canChooseTheTokenModeFourTimes() {
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> citizens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Citizen"))
                .toList();
        assertThat(citizens).hasSize(4);
        for (Permanent citizen : citizens) {
            assertThat(citizen.getCard().getPower()).isEqualTo(2);
            assertThat(citizen.getCard().getToughness()).isEqualTo(2);
            assertThat(citizen.getCard().getColors())
                    .containsExactlyInAnyOrder(
                            com.github.laxika.magicalvibes.model.CardColor.WHITE,
                            com.github.laxika.magicalvibes.model.CardColor.BLUE,
                            com.github.laxika.magicalvibes.model.CardColor.BLACK,
                            com.github.laxika.magicalvibes.model.CardColor.RED,
                            com.github.laxika.magicalvibes.model.CardColor.GREEN);
        }
    }

    @Test
    void canReturnFourPermanentCards() {
        Card first = new Plains();
        Card second = new Plains();
        Card third = new Plains();
        Card fourth = new GrizzlyBears();
        Card instant = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second, third, fourth, instant)));
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castSorcery(player1, 0, 20);
        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, first.getName());
        harness.assertInHand(player1, second.getName());
        harness.assertInHand(player1, third.getName());
        harness.assertInHand(player1, fourth.getName());
        harness.assertInGraveyard(player1, instant.getName());
    }

    @Test
    void allFourModesResolveTogether() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card permanent = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(permanent)));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castSorcery(player1, 0, 14);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        harness.assertInHand(player1, permanent.getName());
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Citizen")))
                .hasSize(1);
    }

    @Test
    void cannotTargetNonPermanentCard() {
        Card first = new Plains();
        Card second = new Plains();
        Card third = new Plains();
        Card fourth = new Plains();
        Card instant = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second, third, fourth, instant)));
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castSorcery(player1, 0, 20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(first.getId(), second.getId(), third.getId(), fourth.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
