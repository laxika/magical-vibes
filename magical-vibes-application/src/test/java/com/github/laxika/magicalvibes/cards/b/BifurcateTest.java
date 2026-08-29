package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BifurcateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a same-named permanent card from the library onto the battlefield")
    void putsSameNamedPermanentOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card sameNamedSorcery = new Card();
        sameNamedSorcery.setName("Grizzly Bears");
        sameNamedSorcery.setType(CardType.SORCERY);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(sameNamedSorcery, new GrizzlyBears()));

        harness.setHand(player1, List.of(new Bifurcate()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
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
