package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.k.KnightOfCliffhaven;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmergeUnscathed.class, KnightOfCliffhaven.class, Vendetta.class})
class EmergeUnscathedTest extends BaseCardTest {

    @Test
    void protectsTargetCreatureAndExilesForRebound() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        EmergeUnscathed card = new EmergeUnscathed();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.handleListChoice(player1, "RED");

        assertThat(bear.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundOffersOneFreeCastAtNextUpkeep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        EmergeUnscathed card = new EmergeUnscathed();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.handleListChoice(player1, "RED");
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Emerge Unscathed");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesTheCardExiled() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        EmergeUnscathed card = new EmergeUnscathed();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.handleListChoice(player1, "RED");
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Emerge Unscathed");
    }

    @Test
    void cannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new EmergeUnscathed()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Emerge Unscathed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedTargetPreventsResolutionAndRebound() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        EmergeUnscathed card = new EmergeUnscathed();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight of Cliffhaven");
        harness.assertInGraveyard(player1, "Emerge Unscathed");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void protectionStopsRemovalAlreadyOnTheStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new EmergeUnscathed()));
        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Knight of Cliffhaven");
        harness.assertInGraveyard(player2, "Vendetta");
        harness.assertLife(player2, 20);
    }

    @Test
    void canChooseWhiteAndProtectionExpiresAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new EmergeUnscathed()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "WHITE");

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.WHITE);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundCanChooseADifferentCreatureAndColor() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new EmergeUnscathed()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.handleListChoice(player1, "RED");
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(other.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
        assertThat(original.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.GREEN);
        harness.assertInGraveyard(player1, "Emerge Unscathed");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundWithoutALegalTargetLeavesTheCardExiledWithoutAnotherOffer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KnightOfCliffhaven());
        EmergeUnscathed card = new EmergeUnscathed();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "RED");
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertNotOnBattlefield(player1, "Knight of Cliffhaven");
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
        harness.assertNotInGraveyard(player1, "Emerge Unscathed");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
