package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.cards.s.ScanTheClouds;
import com.github.laxika.magicalvibes.cards.s.SongOfTotentanz;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempestHart.class, ScanTheClouds.class, JacesIngenuity.class, GrizzlyBears.class, SongOfTotentanz.class})
class TempestHartTest extends BaseCardTest {

    @Test
    void castingSpellWithManaValueFivePutsCounterOnTempestHart() {
        Permanent hart = addCreatureReady(player1, new TempestHart());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingSpellWithManaValueLessThanFiveDoesNotTrigger() {
        Permanent hart = addCreatureReady(player1, new TempestHart());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void adventureDrawsTwoDiscardsTwoAndExilesTheCard() {
        TempestHart card = new TempestHart();
        harness.setHand(player1, List.of(card, new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5})
    void chosenXCountsTowardTheSpellManaValue(int x) {
        Permanent hart = addCreatureReady(player1, new TempestHart());
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        harness.castSorcery(player1, 0, x);
        resolveAllTriggers();

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(x >= 4 ? 1 : 0);
        assertThat(countPermanents(player1, "Rat")).isEqualTo(x);
    }

    @Test
    void opponentsSpellDoesNotPutACounterOnTempestHart() {
        Permanent hart = addCreatureReady(player1, new TempestHart());
        harness.setHand(player2, List.of(new JacesIngenuity()));
        harness.setLibrary(player2, List.of(new TempestHart(), new TempestHart(), new TempestHart()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(hart.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    void adventureCanDiscardTheDrawnCardsAndCreatureCanBeCastFromExile() {
        TempestHart card = new TempestHart();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new TempestHart(), new TempestHart()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castFromExile(player1, card.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tempest Hart");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(findPermanent(player1, "Tempest Hart").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }
}
