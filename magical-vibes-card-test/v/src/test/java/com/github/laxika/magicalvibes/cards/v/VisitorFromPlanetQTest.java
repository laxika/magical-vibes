package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.w.WolfirAvenger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({VisitorFromPlanetQ.class, WolfirAvenger.class, GrizzlyBears.class, Forest.class})
class VisitorFromPlanetQTest extends BaseCardTest {

    @Test
    @DisplayName("Owned creature cards with flash are instants")
    void grantsInstantTypeToOwnedFlashCreaturePermanents() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        Permanent avenger = addCreatureReady(player2, owned(new WolfirAvenger(), player1));

        assertThat(gqs.getEffectiveCardTypes(gd, avenger)).contains(CardType.INSTANT);
    }

    @Test
    @DisplayName("A two-card-type spell offers draw then discard")
    void offersLootForFlashCreatureSpell() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        Card discarded = owned(new GrizzlyBears(), player1);
        Card avenger = owned(new WolfirAvenger(), player1);
        Card drawn = owned(new Forest(), player1);
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, new ArrayList<>(List.of(avenger, discarded)));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("A creature spell without flash does not trigger")
    void doesNotTriggerForCreatureWithoutFlash() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        harness.setHand(player1, List.of(owned(new GrizzlyBears(), player1)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining the loot neither draws nor discards")
    void mayDeclineLoot() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        Card kept = owned(new GrizzlyBears(), player1);
        Card drawn = owned(new Forest(), player1);
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(owned(new WolfirAvenger(), player1), kept));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty hand discards the card just drawn")
    void lootsWithInitiallyEmptyHand() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        Card drawn = owned(new Forest(), player1);
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(owned(new WolfirAvenger(), player1)));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's two-type spell does not trigger the Visitor")
    void doesNotTriggerForOpponentSpell() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        harness.setHand(player2, List.of(owned(new VisitorFromPlanetQ(), player2)));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Visitor from Planet Q");
    }

    @Test
    @CardUsed(NarsetParterOfVeils.class)
    @DisplayName("A prevented draw does not cause a discard")
    void doesNotDiscardWhenDrawIsPrevented() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        harness.addToBattlefield(player2, owned(new NarsetParterOfVeils(), player2));
        Card firstDraw = owned(new Forest(), player1);
        Card preventedDraw = owned(new Forest(), player1);
        Card kept = owned(new GrizzlyBears(), player1);
        harness.setLibrary(player1, List.of(firstDraw, preventedDraw));
        harness.setHand(player1, List.of(owned(new WolfirAvenger(), player1), kept));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(preventedDraw);
    }

    @Test
    @DisplayName("The instant type grant applies to owned flash creatures in every nonbattlefield zone")
    void grantsInstantTypeOutsideBattlefield() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        Card handCard = owned(new WolfirAvenger(), player1);
        Card libraryCard = owned(new WolfirAvenger(), player1);
        Card graveyardCard = owned(new WolfirAvenger(), player1);
        Card exiledCard = owned(new WolfirAvenger(), player1);
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exiledCard));

        for (Card card : List.of(handCard, libraryCard, graveyardCard, exiledCard)) {
            assertThat(gqs.cardHasType(card, CardType.INSTANT, gd, player1.getId())).isTrue();
            assertThat(gqs.cardHasType(card, CardType.CREATURE, gd, player1.getId())).isTrue();
        }
    }

    @Test
    @DisplayName("Controlling an opponent-owned flash creature does not grant it the instant type")
    void doesNotGrantTypeToOpponentOwnedCreature() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        Permanent avenger = addCreatureReady(player1, owned(new WolfirAvenger(), player2));
        Permanent bears = addCreatureReady(player1, owned(new GrizzlyBears(), player1));

        assertThat(gqs.getEffectiveCardTypes(gd, avenger)).doesNotContain(CardType.INSTANT);
        assertThat(gqs.getEffectiveCardTypes(gd, bears)).doesNotContain(CardType.INSTANT);
    }

    @Test
    @DisplayName("Casting another Visitor triggers even without flash")
    void triggersForNaturallyTwoTypeSpell() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        harness.setHand(player1, List.of(owned(new VisitorFromPlanetQ(), player1)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A Visitor does not trigger from its own casting")
    void doesNotTriggerFromItsOwnCasting() {
        harness.setHand(player1, List.of(owned(new VisitorFromPlanetQ(), player1)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Visitor from Planet Q");
    }

    @Test
    @DisplayName("A borrowed flash creature spell retains its owner's type grants")
    void doesNotGrantInstantTypeToOpponentOwnedSpell() {
        harness.addToBattlefield(player1, owned(new VisitorFromPlanetQ(), player1));
        Card borrowed = owned(new WolfirAvenger(), player2);
        harness.setHand(player1, List.of(borrowed));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Wolfir Avenger");
    }
    private <T extends Card> T owned(T card, com.github.laxika.magicalvibes.model.Player owner) {
        card.setOwnerId(owner.getId());
        return card;
    }
}
