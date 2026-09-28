package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WasteManagement.class, GrizzlyBears.class, Shock.class})
class WasteManagementTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two cards from one graveyard and creates a Rogue for each creature card")
    void exilesSelectedCardsAndCreatesRoguesForCreatures() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card untouchedCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature, noncreature, untouchedCreature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addBaseMana();

        harness.castInstant(player1, 0, List.of(creature.getId(), noncreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), noncreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouchedCreature);
        assertRogues(1);
    }

    @Test
    @DisplayName("When kicked, exiles the target player's graveyard and counts only creature cards")
    void kickedExilesEntireGraveyardAndCountsCreatures() {
        Card firstCreature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(firstCreature, noncreature, secondCreature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        firstCreature.getId(), noncreature.getId(), secondCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertRogues(2);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void assertRogues(int count) {
        List<Permanent> rogues = findPermanents(player1, "Rogue");
        assertThat(rogues).hasSize(count);
        assertThat(rogues).allSatisfy(rogue -> {
            assertThat(rogue.getCard().isToken()).isTrue();
            assertThat(rogue.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(rogue.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(rogue.getCard().getSubtypes()).containsExactly(CardSubtype.ROGUE);
            assertThat(rogue.getCard().getPower()).isEqualTo(2);
            assertThat(rogue.getCard().getToughness()).isEqualTo(2);
        });
    }
}
