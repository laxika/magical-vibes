package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VigeanIntuition.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class VigeanIntuitionTest extends BaseCardTest {

    @Test
    void putsCardsOfChosenTypeIntoHandAndTheRestIntoGraveyard() {
        Card forest = new Forest();
        Card island = new Island();
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(forest, island, creature, instant));
        harness.setHand(player1, List.of(new VigeanIntuition()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
