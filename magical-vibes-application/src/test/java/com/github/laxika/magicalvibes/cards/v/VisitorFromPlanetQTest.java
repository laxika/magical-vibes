package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

    private <T extends Card> T owned(T card, com.github.laxika.magicalvibes.model.Player owner) {
        card.setOwnerId(owner.getId());
        return card;
    }
}
