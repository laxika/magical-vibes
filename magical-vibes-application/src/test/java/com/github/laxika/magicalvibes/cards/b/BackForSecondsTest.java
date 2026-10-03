package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FellHorseman;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BackForSeconds.class, Gingerbrute.class, Mintstrosity.class, HamletGlutton.class, FellHorseman.class})
class BackForSecondsTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCreatureCardsToHand() {
        Card lowManaValueCreature = new Mintstrosity();
        Card highManaValueCreature = new HamletGlutton();
        harness.setGraveyard(player1, List.of(lowManaValueCreature, highManaValueCreature));
        harness.castFromHand(player1, new BackForSeconds(), "{2}{B}");
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(lowManaValueCreature.getId(), highManaValueCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(lowManaValueCreature.getId(), highManaValueCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mintstrosity");
        harness.assertInHand(player1, "Hamlet Glutton");
        harness.assertNotInGraveyard(player1, "Mintstrosity");
        harness.assertNotInGraveyard(player1, "Hamlet Glutton");
    }

    @Test
    void bargainedCastMayPutOneLowManaValueCreatureOntoBattlefield() {
        Card lowManaValueCreature = new Mintstrosity();
        Card highManaValueCreature = new HamletGlutton();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setGraveyard(player1, List.of(lowManaValueCreature, highManaValueCreature));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.handleMultipleCardsChosen(player1, List.of(lowManaValueCreature.getId(), highManaValueCreature.getId()));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(lowManaValueCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(lowManaValueCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mintstrosity");
        harness.assertInHand(player1, "Hamlet Glutton");
        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.assertInGraveyard(player1, "Back for Seconds");
    }

    @Test
    void bargainedCastCanDeclineBattlefieldReplacement() {
        Card lowManaValueCreature = new Mintstrosity();
        Card highManaValueCreature = new HamletGlutton();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setGraveyard(player1, List.of(lowManaValueCreature, highManaValueCreature));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.handleMultipleCardsChosen(player1, List.of(lowManaValueCreature.getId(), highManaValueCreature.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mintstrosity");
        harness.assertInHand(player1, "Hamlet Glutton");
        harness.assertNotOnBattlefield(player1, "Mintstrosity");
    }

    @Test
    void canChooseZeroTargetsWithCreaturesInGraveyard() {
        Card creature = new Mintstrosity();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new BackForSeconds(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mintstrosity");
        harness.assertNotInHand(player1, "Mintstrosity");
        harness.assertInGraveyard(player1, "Back for Seconds");
    }

    @Test
    void canCastWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new BackForSeconds(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Back for Seconds");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void targetsOnlyCreatureCardsInYourGraveyardAndCanReturnJustOne() {
        Card creature = new Mintstrosity();
        Card unchosen = new HamletGlutton();
        Card noncreature = new BackForSeconds();
        Card opposingCreature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(creature, unchosen, noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.castFromHand(player1, new BackForSeconds(), "{2}{B}");
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), unchosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mintstrosity");
        harness.assertInGraveyard(player1, "Hamlet Glutton");
        harness.assertInGraveyard(player2, "Gingerbrute");
        harness.assertNotOnBattlefield(player1, "Mintstrosity");
    }

    @Test
    void bargainedCastReturnsHighManaValueTargetToHandWithoutReplacementChoice() {
        Card creature = new HamletGlutton();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hamlet Glutton");
        harness.assertNotOnBattlefield(player1, "Hamlet Glutton");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bargainedCastCanPutManaValueFourCreatureOntoBattlefield() {
        Card creature = new FellHorseman();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fell Horseman");
        harness.assertNotInHand(player1, "Fell Horseman");
        harness.assertNotInGraveyard(player1, "Fell Horseman");
    }

    @Test
    void creatureSacrificedForBargainIsNotAnAvailableTarget() {
        Card creature = new Mintstrosity();
        Card sacrificedCard = new Gingerbrute();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, sacrificedCard);
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.validCardIds()).doesNotContain(sacrificedCard.getId());
    }

    @Test
    void returnsRemainingLegalTargetWhenOtherTargetLeavesGraveyard() {
        Card remaining = new Mintstrosity();
        Card removed = new HamletGlutton();
        harness.setGraveyard(player1, List.of(remaining, removed));
        harness.castFromHand(player1, new BackForSeconds(), "{2}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), removed.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mintstrosity");
        harness.assertNotInHand(player1, "Hamlet Glutton");
        harness.assertInGraveyard(player1, "Back for Seconds");
    }

    @Test
    void bargainedCastPutsOnlyOneOfTwoEligibleTargetsOntoBattlefield() {
        Card first = new Mintstrosity();
        Card second = new FellHorseman();
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new BackForSeconds()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fell Horseman");
        harness.assertInHand(player1, "Mintstrosity");
        harness.assertNotOnBattlefield(player1, "Mintstrosity");
        harness.assertNotInHand(player1, "Fell Horseman");
    }
}
