package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WindingWay.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class WindingWayTest extends BaseCardTest {

    @Test
    void offersOnlyCreatureAndLandChoices() {
        cast(List.of(new GrizzlyBears(), new Forest(), new Shock(), new Island()));

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(CardType.CREATURE.name(), CardType.LAND.name());
    }

    @Test
    void choosingCreaturePutsCreaturesIntoHandAndTheRestIntoGraveyard() {
        Card creature1 = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        Card creature2 = new GrizzlyBears();

        cast(List.of(creature1, land, instant, creature2));
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature1, creature2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void choosingLandPutsLandsIntoHandAndTheRestIntoGraveyard() {
        Card creature = new GrizzlyBears();
        Card land1 = new Forest();
        Card instant = new Shock();
        Card land2 = new Island();

        cast(List.of(creature, land1, instant, land2));
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land1, land2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WindingWay()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
