package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningElemental;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacredGuide.class, LightningElemental.class, TrainedArmodon.class, SeleniaDarkAngel.class})
class SacredGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first white card into hand and exiles the rest")
    void putsWhiteCardToHandRestExiled() {
        addCreatureReady(player1, new SacredGuide());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card redCard = new LightningElemental();
        Card greenCard = new TrainedArmodon();
        Card whiteCard = new SeleniaDarkAngel();
        Card leftover = new LightningElemental();
        harness.setLibrary(player1, List.of(redCard, greenCard, whiteCard, leftover));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(whiteCard);
        assertThat(exiledCards(gd)).containsExactly(redCard, greenCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(leftover);
    }

    @Test
    @DisplayName("Sacrifices itself as a cost")
    void sacrificesItselfAsCost() {
        addCreatureReady(player1, new SacredGuide());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setLibrary(player1, List.of(new TrainedArmodon()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SacredGuide);
    }

    @Test
    @DisplayName("Exiles the whole library when no white card is revealed")
    void noWhiteCardExilesEntireLibrary() {
        addCreatureReady(player1, new SacredGuide());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card redCard = new LightningElemental();
        Card greenCard = new TrainedArmodon();
        harness.setLibrary(player1, List.of(redCard, greenCard));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(exiledCards(gd)).containsExactly(redCard, greenCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new SacredGuide());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setLibrary(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(exiledCards(gd)).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new SacredGuide());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private List<Card> exiledCards(GameData gd) {
        return gd.exiledCards.stream().map(ExiledCardEntry::card).toList();
    }
}
