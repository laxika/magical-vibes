package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.h.Helitrooper;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeneralLeoCristophe.class, GrizzlyBears.class, ThunderingGiant.class,
        Helitrooper.class, MindStone.class, Cloudshift.class})
class GeneralLeoCristopheTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value 3 or less and gets counters for each creature controlled")
    void returnsCreatureAndCountsAllControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        castGeneral();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returnedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        Permanent general = findPermanent(player1, "General Leo Cristophe");
        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The up-to-one return may be declined and still counts creatures")
    void mayDeclineReturn() {
        Card returnedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCard));
        castGeneral();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        Permanent general = findPermanent(player1, "General Leo Cristophe");
        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards with mana value 3 or less are valid return targets")
    void filtersInvalidGraveyardCards() {
        Card tooExpensive = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(tooExpensive));
        castGeneral();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        Permanent general = findPermanent(player1, "General Leo Cristophe");
        assertThat(general.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Thundering Giant");
    }

    private void castGeneral() {
        harness.castFromHand(player1, new GeneralLeoCristophe(), "{4}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("With an empty graveyard, only controlled creatures contribute counters")
    void emptyGraveyardCountsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new Helitrooper());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player2, new Helitrooper());
        castGeneral();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "General Leo Cristophe")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature cards and cards in an opponent's graveyard cannot be returned")
    void excludesNoncreaturesAndOpponentsGraveyard() {
        Card eligible = new Helitrooper();
        Card noncreature = new MindStone();
        Card opponentsCreature = new Helitrooper();
        harness.setGraveyard(player1, List.of(eligible, noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        castGeneral();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Helitrooper");
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player2, "Helitrooper");
        assertThat(findPermanent(player1, "General Leo Cristophe")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing the sole graveyard target prevents the entire ability from resolving")
    void illegalSoleTargetAlsoPreventsCounters() {
        Card target = new Helitrooper();
        harness.setGraveyard(player1, List.of(target));
        castGeneral();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Helitrooper");
        assertThat(findPermanent(player1, "General Leo Cristophe")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Creatures are counted when the ability resolves rather than when it triggers")
    void countsCreaturesAtResolution() {
        Card target = new Helitrooper();
        harness.setGraveyard(player1, List.of(target));
        castGeneral();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.addToBattlefield(player1, new Helitrooper());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "General Leo Cristophe")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The original entry trigger cannot put counters on General Leo after it leaves and returns")
    void originalTriggerDoesNotPutCountersOnReturnedGeneral() {
        Card target = new Helitrooper();
        harness.setGraveyard(player1, List.of(target));
        castGeneral();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        Permanent originalGeneral = findPermanent(player1, "General Leo Cristophe");

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, originalGeneral.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        Permanent returnedGeneral = findPermanent(player1, "General Leo Cristophe");
        assertThat(returnedGeneral.getId()).isNotEqualTo(originalGeneral.getId());
        assertThat(returnedGeneral.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Helitrooper");
        harness.assertNotInGraveyard(player1, "Helitrooper");
        assertThat(returnedGeneral.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
