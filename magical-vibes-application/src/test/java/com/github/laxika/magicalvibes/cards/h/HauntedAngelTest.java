package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.v.Vindicate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntedAngel.class, Vindicate.class})
class HauntedAngelTest extends BaseCardTest {

    @Test
    @DisplayName("When Haunted Angel dies, it is exiled and each opponent creates a 3/3 black Angel with flying")
    void diesExilesItAndGivesOpponentAnAngel() {
        Permanent hauntedAngel = harness.addToBattlefieldAndReturn(player1, new HauntedAngel());

        harness.setHand(player1, List.of(new Vindicate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, hauntedAngel.getId());

        harness.assertInGraveyard(player1, "Haunted Angel");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hauntedAngel.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(hauntedAngel.getCard().getId()));

        List<Permanent> angels = findPermanents(player2, "Angel");
        assertThat(angels).hasSize(1);
        Permanent angel = angels.getFirst();
        assertThat(angel.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(angel.getCard().getPower()).isEqualTo(3);
        assertThat(angel.getCard().getToughness()).isEqualTo(3);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(angel.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(angel.getCard().isToken()).isTrue();
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    @DisplayName("The opponent still creates an Angel if the source has left the graveyard")
    void createsAngelEvenIfSourceLeavesGraveyard() {
        Permanent hauntedAngel = destroyHauntedAngel();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(hauntedAngel.getCard()));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Haunted Angel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player2, "Angel")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    @DisplayName("A source that leaves and returns to the graveyard is not exiled by its old trigger")
    void doesNotExileNewGraveyardObject() {
        Permanent hauntedAngel = destroyHauntedAngel();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(hauntedAngel.getCard()));
        harness.setHand(player1, List.of());
        gd.markGraveyardEntry(hauntedAngel.getCard());
        harness.setGraveyard(player1, List.of(hauntedAngel.getCard()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Haunted Angel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player2, "Angel")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    private Permanent destroyHauntedAngel() {
        Permanent hauntedAngel = harness.addToBattlefieldAndReturn(player1, new HauntedAngel());
        harness.setHand(player1, List.of(new Vindicate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, hauntedAngel.getId());
        harness.assertInGraveyard(player1, "Haunted Angel");
        return hauntedAngel;
    }
}
