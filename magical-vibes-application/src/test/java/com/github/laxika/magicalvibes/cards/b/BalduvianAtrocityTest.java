package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.InThrallToThePit;
import com.github.laxika.magicalvibes.cards.m.MossbeardAncient;
import com.github.laxika.magicalvibes.cards.t.TimelyInterference;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalduvianAtrocity.class, BogBadger.class, MossbeardAncient.class,
        TimelyInterference.class, InThrallToThePit.class})
class BalduvianAtrocityTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotReturnCreature() {
        Card creature = new BogBadger();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Bog Badger");
    }

    @Test
    void kickedReturnsTargetCreatureWithHasteAndSacrificesItAtNextEndStep() {
        Card creature = new BogBadger();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Bog Badger");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Badger");
        harness.assertInGraveyard(player1, "Bog Badger");
    }

    @Test
    void onlyTargetsCreaturesWithManaValueThreeOrLess() {
        Card badger = new BogBadger();
        Card ancient = new MossbeardAncient();
        harness.setGraveyard(player1, List.of(badger, ancient));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(badger.getId());
    }

    @Test
    void excludesNoncreaturesAndOpponentsGraveyard() {
        Card eligible = new BogBadger();
        Card opponentCreature = new BogBadger();
        harness.setGraveyard(player1, List.of(eligible, new TimelyInterference()));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    void kickedWithNoLegalTargetStillEnters() {
        harness.setGraveyard(player1, List.of(new MossbeardAncient(), new TimelyInterference()));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Balduvian Atrocity");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionIsNotReturned() {
        Card creature = new BogBadger();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addKickedMana();
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Badger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void delayedSacrificeUsesTheStackBeforeRemovingCreature() {
        Card creature = new BogBadger();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addKickedMana();
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Bog Badger");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Bog Badger");
        harness.assertInGraveyard(player1, "Bog Badger");
    }

    @Test
    void cannotSacrificeReturnedCreatureControlledByOpponent() {
        Card creature = new BogBadger();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BalduvianAtrocity()));
        addKickedMana();
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new InThrallToThePit()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player2, 0, harness.getPermanentId(player1, "Bog Badger"));
        harness.assertOnBattlefield(player2, "Bog Badger");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.values().stream().flatMap(List::stream))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Bog Badger");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
