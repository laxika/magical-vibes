package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.SphinxOfTheFinalWord;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenyExistence.class, GrizzlyBears.class, MightOfOaks.class, SphinxOfTheFinalWord.class})
class DenyExistenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and exiles it instead of putting it in the graveyard")
    void countersCreatureSpellAndExilesIt() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DenyExistence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Deny Existence");
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new DenyExistence()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, might.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An uncounterable creature is a legal target but is not exiled")
    void doesNotExileUncounterableCreature() {
        SphinxOfTheFinalWord sphinx = new SphinxOfTheFinalWord();
        harness.setHand(player1, List.of(sphinx));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.setHand(player2, List.of(new DenyExistence()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Sphinx of the Final Word");
        harness.assertInGraveyard(player2, "Deny Existence");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sphinx of the Final Word");
    }

    @Test
    @DisplayName("Does nothing when its target has already left the stack")
    void doesNothingWhenTargetHasLeftStack() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DenyExistence(), new DenyExistence()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .containsExactly(bears);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .hasSize(2)
                .allMatch(card -> card instanceof DenyExistence);
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
