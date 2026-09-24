package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellCrumple.class, GrizzlyBears.class, DarkRitual.class})
class SpellCrumpleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell onto the bottom of its owner's library and puts Spell Crumple on the bottom of its owner's library")
    void countersTargetAndPutsBothCardsOnBottomOfOwnersLibraries() {
        GrizzlyBears bears = new GrizzlyBears();
        DarkRitual existingTopCard = new DarkRitual();
        harness.setLibrary(player1, List.of(existingTopCard));
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        SpellCrumple spellCrumple = new SpellCrumple();
        harness.setHand(player2, List.of(spellCrumple));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTopCard, bears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spellCrumple);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Spell Crumple");
        assertThat(gd.stack).isEmpty();
    }
}
