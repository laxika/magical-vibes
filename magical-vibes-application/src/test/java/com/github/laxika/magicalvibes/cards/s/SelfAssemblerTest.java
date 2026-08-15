package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelfAssemblerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may search prompt")
    void enteringCreatesMaySearchPrompt() {
        castSelfAssembler();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability offers only Assembly-Worker creature cards")
    void acceptingOffersMatchingCreatureCards() {
        castSelfAssembler();
        setLibrary(new SelfAssembler(), new ArcboundWorker(), new GrizzlyBears());

        resolveEtbMay(true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered.getFirst().getSubtypes()).contains(CardSubtype.ASSEMBLY_WORKER);
        assertThat(offered.getFirst().getType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Choosing an Assembly-Worker creature puts it into hand")
    void choosingMatchingCreaturePutsItIntoHand() {
        castSelfAssembler();
        setLibrary(new SelfAssembler(), new ArcboundWorker());

        resolveEtbMay(true);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Self-Assembler"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the search")
    void decliningSkipsSearch() {
        castSelfAssembler();
        setLibrary(new SelfAssembler());

        resolveEtbMay(false);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSelfAssembler() {
        harness.setHand(player1, List.of(new SelfAssembler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveEtbMay(boolean accept) {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
