package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunderingArchaic.class, LlanowarElves.class, HillGiant.class, GrizzlyBears.class})
class SunderingArchaicTest extends BaseCardTest {

    @Test
    @DisplayName("Converge exiles an opponent's nonland permanent within the color limit")
    void convergeExilesWithinColorLimit() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SunderingArchaic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID elfCardId = findPermanent(player2, "Llanowar Elves").getCard().getId();
        UUID elfId = harness.getPermanentId(player2, "Llanowar Elves");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(elfId).doesNotContain(giantId);
        harness.handlePermanentChosen(player1, elfId);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(elfCardId)).isNotNull();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Puts a target card from any graveyard on the bottom of its owner's library")
    void tucksCardFromAnyGraveyard() {
        harness.addToBattlefield(player1, new SunderingArchaic());
        Permanent archaic = findPermanent(player1, "Sundering Archaic");
        archaic.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(tucked));
        harness.setLibrary(player2, List.of(new HillGiant()));

        int archaicIndex = gd.playerBattlefields.get(player1.getId()).indexOf(archaic);
        harness.activateAbilityWithGraveyardTargets(player1, archaicIndex, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(tucked.getId());
    }

    @Test
    void repeatedManaOfOneColorDoesNotIncreaseConverge() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new SunderingArchaic()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID ownElfId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(targetId).doesNotContain(bearsId, ownElfId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void fourColorsAllowExilingManaValueFour() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SunderingArchaic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        UUID cardId = findPermanent(player2, "Hill Giant").getCard().getId();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(targetId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(cardId)).isNotNull();
    }

    @Test
    void colorlessPaymentCannotExilePositiveManaValue() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new SunderingArchaic()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sundering Archaic");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSummoningSickArchaicCanTuckOwnGraveyardCard() {
        harness.addToBattlefield(player1, new SunderingArchaic());
        Permanent archaic = findPermanent(player1, "Sundering Archaic");
        archaic.setSummoningSick(true);
        archaic.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card tucked = new GrizzlyBears();
        Card top = new HillGiant();
        harness.setGraveyard(player1, List.of(tucked));
        harness.setLibrary(player1, List.of(top));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, tucked);
        assertThat(archaic.isTapped()).isTrue();
    }

    @Test
    void secondActivationCannotMoveCardAlreadyReturnedToLibrary() {
        harness.addToBattlefield(player1, new SunderingArchaic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Card tucked = new GrizzlyBears();
        Card top = new HillGiant();
        harness.setGraveyard(player2, List.of(tucked));
        harness.setLibrary(player2, List.of(top));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(tucked.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(tucked.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, tucked);
    }
}
