package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CauldronsGift.class, Forest.class, GrizzlyBears.class})
class CauldronsGiftTest extends BaseCardTest {

    @Test
    void adamantMillsFourAndReturnsCreatureWithCounter() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest graveyardLand = new Forest();
        List<Card> milledCards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        CauldronsGift spell = new CauldronsGift();
        harness.setLibrary(player1, milledCards);
        harness.setGraveyard(player1, List.of(creature, graveyardLand));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, graveyardLand,
                milledCards.get(0), milledCards.get(1), milledCards.get(2), milledCards.get(3));
        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardLand,
                milledCards.get(0), milledCards.get(1), milledCards.get(2), milledCards.get(3), spell);
    }

    @Test
    void withoutThreeBlackManaDoesNotMillButStillReturnsCreature() {
        GrizzlyBears creature = new GrizzlyBears();
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new CauldronsGift()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
