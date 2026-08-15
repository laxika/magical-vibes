package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElderDeepFiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoulEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a creature from the top four cards")
    void etbOffersCreatureFromTopFour() {
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new FoulEmissary()));
        harness.setLibrary(player1, List.of(creature, new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(creature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Creates an Eldrazi Horror when sacrificed to cast an emerge spell")
    void createsTokenWhenSacrificedToCastEmergeSpell() {
        Permanent foulEmissary = addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(foulEmissary.getId()));
        assertThat(findPermanents(player1, "Foul Emissary")).isEmpty();

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Eldrazi Horror");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not create a token when sacrificed to cast a non-emerge spell")
    void doesNotCreateTokenForNonEmergeSpell() {
        Permanent foulEmissary = addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), foulEmissary.getId());
        assertThat(findPermanents(player1, "Foul Emissary")).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
    }
}
