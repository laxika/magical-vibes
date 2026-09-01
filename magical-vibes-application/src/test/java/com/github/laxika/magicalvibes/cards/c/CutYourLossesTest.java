package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({CutYourLosses.class, GrizzlyBears.class, LlanowarElves.class})
class CutYourLossesTest extends BaseCardTest {

    @Test
    @DisplayName("Mills half the target player's library, rounded down")
    void millsHalfRoundedDown() {
        harness.setLibrary(player2, libraryOf(9));
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Casualty copies the spell and mills the remaining library separately")
    void casualtyCopiesSpell() {
        Permanent casualtyCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, libraryOf(10));
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Cannot pay casualty with a creature below the required power")
    void rejectsUnderpoweredCasualtyCreature() {
        Permanent casualtyCreature = addCreatureReady(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new CutYourLosses()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, player2.getId(), casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2");
    }

    private List<Card> libraryOf(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
