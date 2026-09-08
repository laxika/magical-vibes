package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Skinshifter;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BebopWarthogWarrior.class, GrizzlyBears.class, Skinshifter.class, Swamp.class})
class BebopWarthogWarriorTest extends BaseCardTest {

    private static final String RHINO_MODE =
            "Until end of turn, this creature becomes a Rhino with base power and toughness 4/4 and gains trample.";

    @Test
    @DisplayName("Gives menace to Rhinos you control, but not other creatures or an opponent's Rhinos")
    void grantsMenaceToYourRhinosOnly() {
        harness.addToBattlefield(player1, new BebopWarthogWarrior());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownRhino = makeRhino(player1);
        Permanent opponentsRhino = makeRhino(player2);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownRhino, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentsRhino, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The menace grant ends when a creature stops being a Rhino")
    void menaceGrantTracksSubtypeChanges() {
        harness.addToBattlefield(player1, new BebopWarthogWarrior());
        Permanent skinshifter = makeRhino(player1);

        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skinshifter.getTransientCreatureTypeOverride()).isNull();
        assertThat(gqs.hasKeyword(gd, skinshifter, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Swampcycling searches for a Swamp and puts it into hand")
    void swampcyclingSearchesForSwamp() {
        BebopWarthogWarrior bebop = new BebopWarthogWarrior();
        GrizzlyBears bears = new GrizzlyBears();
        Swamp swamp = new Swamp();
        harness.setHand(player1, List.of(bebop));
        harness.setLibrary(player1, List.of(bears, swamp));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(swamp);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Bebop, Warthog Warrior");
        harness.assertInHand(player1, "Swamp");
        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);
    }

    private Permanent makeRhino(Player player) {
        Permanent skinshifter = harness.addToBattlefieldAndReturn(player, new Skinshifter());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.GREEN, 1);
        int permanentIndex = gd.playerBattlefields.get(player.getId()).indexOf(skinshifter);
        harness.activateAbility(player, permanentIndex, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player, RHINO_MODE);
        return skinshifter;
    }
}
