package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SamutsSprint;
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

@CardUsed({PlanewideCelebration.class, PrimordialWurm.class, SamutsSprint.class, Plains.class})
class PlanewideCelebrationTest extends BaseCardTest {

    @Test
    void canChooseTheTokenModeFourTimes() {
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

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
        Card fourth = new PrimordialWurm();
        Card instant = new SamutsSprint();
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card permanent = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(permanent)));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castSorcery(player1, 0, 14);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
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
        Card instant = new SamutsSprint();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second, third, fourth, instant)));
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castSorcery(player1, 0, 20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(first.getId(), second.getId(), third.getId(), fourth.getId());
    }

    @Test
    void canChooseTheLifeModeFourTimesWithoutGraveyardTargets() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 34);

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Planewide Celebration");
    }

    @Test
    void repeatedProliferationAllowsDifferentChoicesEachTime() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 30);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Planewide Celebration");
    }

    @Test
    void canChooseTheSamePermanentForAllFourReturnModes() {
        Card permanent = new Plains();
        harness.setGraveyard(player1, List.of(permanent));
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        addMana();

        harness.castSorcery(player1, 0, 20);
        harness.handleMultipleCardsChosen(player1,
                List.of(permanent.getId(), permanent.getId(), permanent.getId(), permanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(permanent);
        harness.assertNotInGraveyard(player1, permanent.getName());
    }

    @Test
    void noModesResolveWhenTheOnlyGraveyardTargetBecomesIllegal() {
        Card permanent = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(permanent)));
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        harness.setLife(player1, 10);
        addMana();

        harness.castSorcery(player1, 0, 14);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(permanent);
        harness.setExile(player1, List.of(permanent));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertNotInHand(player1, permanent.getName());
        harness.assertInGraveyard(player1, "Planewide Celebration");
    }

    @Test
    void remainingLegalReturnTargetAllowsOtherModesToResolve() {
        Card first = new Plains();
        Card second = new PrimordialWurm();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second)));
        harness.setHand(player1, List.of(new PlanewideCelebration()));
        harness.setLife(player1, 10);
        addMana();

        harness.castSorcery(player1, 0, 12);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(first);
        harness.setExile(player1, List.of(first));
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInHand(player1, second.getName());
        harness.assertNotInHand(player1, first.getName());
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Citizen")))
                .hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
