package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NullstoneGargoyle.class, BorosSignet.class, ElvesOfDeepShadow.class})
class NullstoneGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters only the first noncreature spell of the turn across all players")
    void countersFirstNoncreatureSpellGlobally() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());

        castBorosSignet(player2);
        harness.assertInGraveyard(player2, "Boros Signet");

        castBorosSignet(player1);
        harness.assertOnBattlefield(player1, "Boros Signet");
    }

    @Test
    @DisplayName("Creature spells do not count as the first noncreature spell")
    void ignoresCreatureSpells() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());
        harness.setHand(player2, List.of(new ElvesOfDeepShadow(), new BorosSignet()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareCast(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Elves of Deep Shadow");

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Boros Signet");
    }

    @Test
    @DisplayName("A spell cast before Nullstone Gargoyle enters still counts")
    void spellsBeforeEntryCount() {
        castBorosSignet(player2);
        harness.assertOnBattlefield(player2, "Boros Signet");

        harness.addToBattlefield(player1, new NullstoneGargoyle());
        castBorosSignet(player2);

        assertThat(findPermanents(player2, "Boros Signet")).hasSize(2);
    }

    @Test
    @DisplayName("Counters the first noncreature spell of each turn")
    void resetsForNextTurn() {
        harness.addToBattlefield(player1, new NullstoneGargoyle());

        castBorosSignet(player1);
        harness.assertInGraveyard(player1, "Boros Signet");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        castBorosSignet(player2);
        harness.assertInGraveyard(player2, "Boros Signet");
    }

    private void castBorosSignet(Player player) {
        prepareCast(player);
        harness.castFromHand(player, new BorosSignet(), "{2}");
        harness.passBothPriorities();
    }

    private void prepareCast(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
