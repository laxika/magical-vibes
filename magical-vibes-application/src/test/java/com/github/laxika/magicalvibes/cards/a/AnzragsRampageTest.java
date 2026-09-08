package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnzragsRampage.class, GrizzlyBears.class, Ornithopter.class, Shock.class})
class AnzragsRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys opposing artifacts, counts artifacts put into graveyards this turn, and returns the chosen creature")
    void destroysOpposingArtifactsAndReturnsChosenCreature() {
        Permanent earlierArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, earlierArtifact));
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        setLibrary(creature, noncreature);
        harness.setHand(player1, List.of(new AnzragsRampage()));
        addRampageMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(noncreature);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(entered.getId())
                        && action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        advanceToNextEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not exile cards when no artifact was put into a graveyard this turn")
    void doesNothingToTheLibraryWhenNoArtifactsWerePutIntoGraveyards() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        setLibrary(creature, noncreature);
        harness.setHand(player1, List.of(new AnzragsRampage()));
        addRampageMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, noncreature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void addRampageMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void advanceToNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
