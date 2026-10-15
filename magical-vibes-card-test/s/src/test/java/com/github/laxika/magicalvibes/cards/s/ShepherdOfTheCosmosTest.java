package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BindTheMonster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShepherdOfTheCosmos.class, AvatarOfMight.class, BindTheMonster.class,
        Forest.class, GrizzlyBears.class, HolyDay.class})
class ShepherdOfTheCosmosTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target permanent card with mana value 2 or less")
    void returnsLowManaValuePermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castShepherd();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can return a low-mana-value land permanent")
    void returnsLandPermanent() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castShepherd();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("ETB rejects nonpermanents and permanents with mana value greater than 2")
    void rejectsCardsOutsideTheFilter() {
        Card highManaValuePermanent = new AvatarOfMight();
        Card nonpermanent = new HolyDay();
        GrizzlyBears legal = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(highManaValuePermanent, nonpermanent, legal));

        castShepherd();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legal.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(highManaValuePermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB only targets cards in its controller's graveyard")
    void onlyTargetsOwnGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        castShepherd();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        ShepherdOfTheCosmos shepherd = new ShepherdOfTheCosmos();
        harness.setHand(player1, List.of(shepherd));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(shepherd.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, shepherd.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shepherd of the Cosmos");
    }

    @Test
    @DisplayName("Returning an Aura allows choosing a legal creature to enchant")
    void returnsAuraAttachedToChosenCreature() {
        BindTheMonster aura = new BindTheMonster();
        harness.setGraveyard(player1, List.of(aura));

        castShepherd();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        var shepherdId = harness.getPermanentId(player1, "Shepherd of the Cosmos");
        harness.handlePermanentChosen(player1, shepherdId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bind the Monster");
        harness.assertNotInGraveyard(player1, "Bind the Monster");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(aura.getId()))
                .singleElement().satisfies(p -> assertThat(p.getAttachedTo()).isEqualTo(shepherdId));
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("ETB cannot decline its target when a legal permanent is available")
    void requiresAvailableTarget() {
        harness.setGraveyard(player1, List.of(new Forest()));

        castShepherd();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB does not return a target that leaves the graveyard before resolution")
    void doesNotReturnMissingTarget() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castShepherd();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Shepherd of the Cosmos");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A foretold Shepherd cannot be cast on the turn it was foretold")
    void cannotCastOnForetellTurn() {
        ShepherdOfTheCosmos shepherd = new ShepherdOfTheCosmos();
        harness.setHand(player1, List.of(shepherd));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, shepherd.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(shepherd.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castShepherd() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ShepherdOfTheCosmos(), "{4}{W}{W}");
        harness.passBothPriorities();
    }
}
