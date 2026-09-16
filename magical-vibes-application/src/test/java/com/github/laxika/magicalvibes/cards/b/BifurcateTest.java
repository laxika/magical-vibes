package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.h.HickoryWoodlot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bifurcate.class, FreshVolunteers.class, HickoryWoodlot.class})
class BifurcateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a same-named permanent card from the library onto the battlefield")
    void putsSameNamedPermanentOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Card sameNamedSorcery = new Card();
        sameNamedSorcery.setName("Fresh Volunteers");
        sameNamedSorcery.setType(CardType.SORCERY);
        harness.setLibrary(player1, List.of(sameNamedSorcery, new FreshVolunteers()));

        harness.setHand(player1, List.of(new Bifurcate()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Fresh Volunteers");

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Fresh Volunteers")).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target a nontoken creature controlled by an opponent")
    void canTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        harness.setHand(player1, List.of(new Bifurcate()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Fresh Volunteers")).hasSize(1);
        assertThat(findPermanents(player2, "Fresh Volunteers")).containsExactly(target);
    }

    @Test
    @DisplayName("Does not offer a nonpermanent card with the target's name")
    void doesNotOfferNonpermanentSameNamedCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Card sameNamedSorcery = new Card();
        sameNamedSorcery.setName("Fresh Volunteers");
        sameNamedSorcery.setType(CardType.SORCERY);
        HickoryWoodlot unrelated = new HickoryWoodlot();
        harness.setLibrary(player1, List.of(sameNamedSorcery, unrelated));

        harness.setHand(player1, List.of(new Bifurcate()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Fresh Volunteers")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(sameNamedSorcery, unrelated);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HickoryWoodlot());

        harness.setHand(player1, List.of(new Bifurcate()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a token creature")
    void cannotTargetTokenCreature() {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        harness.setHand(player1, List.of(new Bifurcate()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Bear Token")).hasSize(1);
    }
}
