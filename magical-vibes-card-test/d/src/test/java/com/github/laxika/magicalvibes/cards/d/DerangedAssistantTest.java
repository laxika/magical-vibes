package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DerangedAssistant.class, VictimOfNight.class})
class DerangedAssistantTest extends BaseCardTest {

    @Test
    @DisplayName("Can mill the last library card without losing or milling again on resolution")
    void canMillLastLibraryCard() {
        Permanent assistant = addCreatureReady(player1, new DerangedAssistant());
        Card lastCard = new DerangedAssistant();
        harness.setLibrary(player1, List.of(lastCard));
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(assistant.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lastCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
    }

    @Test
    @DisplayName("The ability resolves after the Assistant is destroyed in response")
    void resolvesAfterSourceIsDestroyed() {
        Permanent assistant = addCreatureReady(player1, new DerangedAssistant());
        Card milledCard = new DerangedAssistant();
        harness.setLibrary(player1, List.of(milledCard, new DerangedAssistant()));
        harness.setHand(player2, List.of(new VictimOfNight()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, assistant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deranged Assistant");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard, assistant.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Activating ability taps, mills 1 card, and adds {C}")
    void activateAbilityMillsAndAddsColorless() {
        Permanent assistant = addCreatureReady(player1, new DerangedAssistant());

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        assertThat(assistant.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The milled card goes to the graveyard")
    void milledCardGoesToGraveyard() {
        addCreatureReady(player1, new DerangedAssistant());

        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).extracting(Card::getName).contains(topCard.getName());
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new DerangedAssistant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new DerangedAssistant());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with empty library")
    void cannotActivateWithEmptyLibrary() {
        Permanent assistant = addCreatureReady(player1, new DerangedAssistant());
        harness.setLibrary(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to mill");

        assertThat(assistant.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
