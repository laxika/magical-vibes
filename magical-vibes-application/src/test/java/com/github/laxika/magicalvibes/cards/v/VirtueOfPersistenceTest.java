package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LocthwainScorn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VirtueOfPersistence.class, LocthwainScorn.class, GrizzlyBears.class, HamletGlutton.class, Island.class})
class VirtueOfPersistenceTest extends BaseCardTest {

    @Test
    void adventureShrinksCreatureGainsLifeAndExilesCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        VirtueOfPersistence card = new VirtueOfPersistence();
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        VirtueOfPersistence card = new VirtueOfPersistence();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentFaceReturnsTargetCreatureFromAnyGraveyard() {
        VirtueOfPersistence card = new VirtueOfPersistence();
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, card);
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard().getId()).isEqualTo(creature.getId());
        assertThat(returned).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(cardInGraveyard -> cardInGraveyard.getId().equals(creature.getId()));
    }

    @Test
    void enchantmentFaceDoesNotTriggerWithoutCreatureCardInAnyGraveyard() {
        harness.addToBattlefield(player1, new VirtueOfPersistence());
        harness.setGraveyard(player2, List.of(new Island()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enchantmentFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        VirtueOfPersistence card = new VirtueOfPersistence();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureShrinkExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HamletGlutton());
        harness.setHand(player1, List.of(new VirtueOfPersistence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.assertLife(player1, 22);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void illegalAdventureTargetPreventsLifeGainAndAdventureExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HamletGlutton());
        VirtueOfPersistence card = new VirtueOfPersistence();
        harness.setHand(player1, List.of(card));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Virtue of Persistence");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void upkeepReturnsOwnCreatureAndResolvesItsEnterTrigger() {
        Card creature = new HamletGlutton();
        harness.addToBattlefield(player1, new VirtueOfPersistence());
        harness.setGraveyard(player1, List.of(creature));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hamlet Glutton");
        harness.assertNotInGraveyard(player1, "Hamlet Glutton");
        harness.assertLife(player1, 23);
        assertThat(findPermanent(player1, "Hamlet Glutton").isTapped()).isFalse();
    }

    @Test
    void enchantmentDoesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new VirtueOfPersistence());
        harness.setGraveyard(player2, List.of(new HamletGlutton()));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Hamlet Glutton");
        harness.assertNotOnBattlefield(player1, "Hamlet Glutton");
    }

    @Test
    void upkeepDoesNotReturnAnotherCreatureWhenChosenTargetLeavesGraveyard() {
        Card chosen = new HamletGlutton();
        Card remaining = new HamletGlutton();
        harness.addToBattlefield(player1, new VirtueOfPersistence());
        harness.setGraveyard(player2, List.of(chosen, remaining));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setExile(player2, List.of(chosen));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hamlet Glutton");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
    }
}
