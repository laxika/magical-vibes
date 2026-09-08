package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NullstoneGargoyle.class, GrizzlyBears.class, MindStone.class})
class NullstoneGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters only the first noncreature spell of the turn across all players")
    void countersFirstNoncreatureSpellGlobally() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());

        castMindStone(player2);
        harness.assertInGraveyard(player2, "Mind Stone");

        castMindStone(player1);
        harness.assertOnBattlefield(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Creature spells do not count as the first noncreature spell")
    void ignoresCreatureSpells() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());
        harness.setHand(player2, List.of(new GrizzlyBears(), new MindStone()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareCast(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mind Stone");
    }

    @Test
    @DisplayName("A spell cast before Nullstone Gargoyle enters still counts")
    void spellsBeforeEntryCount() {
        castMindStone(player2);
        harness.assertOnBattlefield(player2, "Mind Stone");

        harness.addToBattlefield(player1, new NullstoneGargoyle());
        castMindStone(player2);

        assertThat(findPermanents(player2, "Mind Stone")).hasSize(2);
    }

    private void castMindStone(Player player) {
        harness.setHand(player, List.of(new MindStone()));
        harness.addMana(player, ManaColor.COLORLESS, 2);
        prepareCast(player);
        harness.castArtifact(player, 0);
        harness.passBothPriorities();
    }

    private void prepareCast(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
