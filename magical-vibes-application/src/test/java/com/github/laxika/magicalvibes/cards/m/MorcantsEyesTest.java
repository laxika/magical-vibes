package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MorcantsEyesTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at the beginning of your upkeep")
    void surveilsAtUpkeep() {
        harness.addToBattlefield(player1, new MorcantsEyes());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing creates one 2/2 Elf token per Elf card in the graveyard")
    void createsTokensPerElfCardAndSacrifices() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MorcantsEyes());
        harness.setGraveyard(player1, List.of(elfCard("Elvish Visionary"), elfCard("Imperious Perfect"), nonElfCard()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        Permanent eyes = findPermanent(player1, "Morcant's Eyes");
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(eyes);
        harness.activateAbility(player1, permanentIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf"))
                .hasSize(3)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELF);
                });

        harness.assertNotOnBattlefield(player1, "Morcant's Eyes");
        harness.assertInGraveyard(player1, "Morcant's Eyes");
    }

    private static Card elfCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.ELF));
        return card;
    }

    private static Card nonElfCard() {
        Card card = new Card();
        card.setName("Grizzly Bears");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }
}
