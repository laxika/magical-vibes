package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YunasDecision.class, GrizzlyBears.class, Forest.class, Shock.class})
class YunasDecisionTest extends BaseCardTest {

    @Test
    @DisplayName("Continue the Pilgrimage sacrifices, draws, and offers a creature and a land")
    void continueThePilgrimage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player1, List.of(new YunasDecision(), creature, land));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Find Another Way returns one or two permanent cards from the graveyard")
    void findAnotherWayReturnsUpToTwoPermanents() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(creature, land, instant));
        harness.setHand(player1, List.of(new YunasDecision()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Without a creature to sacrifice, the first mode neither draws nor puts cards into play")
    void noCreatureToSacrifice() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YunasDecision(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Yuna's Decision");
    }

    @Test
    @DisplayName("The player may decline both cards after sacrificing and drawing")
    void mayDeclineBothCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YunasDecision(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The drawn card may be put onto the battlefield, with the land declined")
    void mayPutDrawnCreatureOnly() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawnCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new YunasDecision(), new Forest()));
        harness.setLibrary(player1, List.of(drawnCreature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).containsExactly(drawnCreature.getId());
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature must wait for the land selection so both enter together")
    void creatureDoesNotEnterBeforeLandIsChosen() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new YunasDecision(), creature, new Forest()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).doesNotContain(creature.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Find Another Way can return only one permanent and leaves the opponent's graveyard alone")
    void findAnotherWayReturnsOnePermanent() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new YunasDecision()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing continues the spell immediately without a separate trigger")
    void sacrificeContinuesResolutionImmediately() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YunasDecision(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertInHand(player1, "Shock");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.assertInGraveyard(player1, "Yuna's Decision");
    }

    @Test
    @DisplayName("The creature may be declined while the land is put onto the battlefield")
    void mayPutLandOnly() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YunasDecision(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Find Another Way returns the remaining target if the other leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        harness.setHand(player1, List.of(new YunasDecision()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.setGraveyard(player1, List.of(land));
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Yuna's Decision");
    }
}
