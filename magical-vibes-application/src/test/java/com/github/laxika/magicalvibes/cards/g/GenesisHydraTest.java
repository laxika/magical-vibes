package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.CarnifexDemon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenesisHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Cast trigger reveals X cards; a nonland permanent goes to the battlefield and the rest are shuffled back")
    void castTriggerPutsPermanentOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card blaze = new Blaze();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(bears, forest, blaze));

        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3); // {X}{G}{G} with X=3

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, blaze);

        harness.passBothPriorities();
        Permanent hydra = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Genesis Hydra"))
                .findFirst().orElse(null);
        assertThat(hydra).isNotNull();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lands and cards with mana value greater than X are not offered")
    void ineligibleCardsAreShuffledBackWithoutAPrompt() {
        Card forest = new Forest();
        Card demon = new CarnifexDemon();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, demon));

        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2); // X=2, so the demon is too expensive

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, demon);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Carnifex Demon");
    }

    @Test
    @DisplayName("Casting with X=0 reveals nothing and the hydra dies as a 0/0")
    void xZeroRevealsNothingAndHydraDies() {
        harness.setHand(player1, List.of(new GenesisHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Genesis Hydra");
    }
}
