package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtractFromDarkness.class, Forest.class, GrizzlyBears.class, HolyDay.class})
class ExtractFromDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Each player mills two cards, then a creature from any graveyard is reanimated")
    void millsEachPlayerThenReanimatesCreature() {
        Card ownMilledCard = new Forest();
        Card ownSecondMilledCard = new HolyDay();
        Card opponentMilledCard = new Forest();
        Card opponentSecondMilledCard = new HolyDay();
        Card creature = new GrizzlyBears();

        harness.setLibrary(player1, List.of(ownMilledCard, ownSecondMilledCard));
        harness.setLibrary(player2, List.of(opponentMilledCard, opponentSecondMilledCard));
        harness.setGraveyard(player2, List.of(creature));
        castExtractFromDarkness();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(ownMilledCard.getId(), ownSecondMilledCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        creature.getId(), opponentMilledCard.getId(), opponentSecondMilledCard.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when neither graveyard contains a creature card")
    void doesNothingWithoutCreatureCard() {
        harness.setLibrary(player1, List.of(new Forest(), new HolyDay()));
        harness.setLibrary(player2, List.of(new Forest(), new HolyDay()));
        harness.setHand(player1, List.of(new ExtractFromDarkness()));
        addManaForExtractFromDarkness();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getClass)
                .containsExactlyInAnyOrder(ExtractFromDarkness.class, Forest.class, HolyDay.class);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getClass)
                .containsExactlyInAnyOrder(Forest.class, HolyDay.class);
    }

    private void castExtractFromDarkness() {
        harness.setHand(player1, List.of(new ExtractFromDarkness()));
        addManaForExtractFromDarkness();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void addManaForExtractFromDarkness() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
