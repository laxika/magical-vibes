package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelebrateTheMountainKing.class, Forest.class, GrizzlyBears.class, Naturalize.class})
class CelebrateTheMountainKingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one nonland permanent per opponent")
    void etbExilesOpponentPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(List.of(bear.getId()), new GrizzlyBears(), new Forest());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Each opponent contributes at most one target")
    void cannotTargetTwoPermanentsControlledByTheSameOpponent() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast(new GrizzlyBears(), new Forest());

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Recruit creates a Soldier after discarding a nonland card")
    void recruitCreatesSoldierForNonlandDiscard() {
        castAndResolve(List.of(), new GrizzlyBears(), new Forest());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Human Soldier"));
    }

    @Test
    @DisplayName("Recruit does not create a Soldier after discarding a land card")
    void recruitDoesNotCreateSoldierForLandDiscard() {
        castAndResolve(List.of(), new Forest(), new GrizzlyBears());

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Human Soldier"));
    }

    @Test
    @DisplayName("Recruit creates a Human Soldier token")
    void recruitTokenIsHumanSoldier() {
        castAndResolve(List.of(), new GrizzlyBears(), new Forest());

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Exile and recruit enter abilities go on the stack separately")
    void enterAbilitiesAreSeparateTriggers() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast(new GrizzlyBears(), new Forest());
        harness.castEnchantment(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("The controller may decline to exile an available opposing permanent")
    void mayChooseNoTargetsWithOpponentPermanentAvailable() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve(List.of(), new GrizzlyBears(), new Forest());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Recruit may discard the card just drawn and creates its token during resolution")
    void mayDiscardDrawnCard() {
        prepareCast(new Forest(), new GrizzlyBears());
        harness.castEnchantment(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The exile ability cannot target a land")
    void cannotTargetOpponentLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast(new GrizzlyBears(), new Forest());

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The exile ability cannot target the controller's permanent")
    void cannotTargetOwnPermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareCast(new GrizzlyBears(), new Forest());

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target is not exiled if the enchantment leaves before its enter ability resolves")
    void sourceLeavingBeforeResolutionPreventsExile() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast(new GrizzlyBears(), new Forest());
        harness.castEnchantment(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Celebrate the Mountain-king"));
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled permanents return when the enchantment leaves")
    void exiledPermanentReturnsWhenSourceLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(List.of(bear.getId()), new GrizzlyBears(), new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        UUID sourceId = harness.getPermanentId(player1, "Celebrate the Mountain-king");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private void castAndResolve(List<UUID> targetIds, com.github.laxika.magicalvibes.model.Card discardedCard,
                                com.github.laxika.magicalvibes.model.Card drawnCard) {
        prepareCast(discardedCard, drawnCard);
        harness.castEnchantment(player1, 0, targetIds);
        for (int i = 0; i < 8 && (!gd.stack.isEmpty()
                || gd.interaction.activeInteraction() instanceof PendingInteraction.MultiPermanentChoice
                || gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice
                || gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice); i++) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiPermanentChoice) {
                harness.handleMultiplePermanentsChosen(player1, targetIds);
            } else if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice
                    && targetIds.isEmpty()) {
                harness.handlePermanentChosen(player1, player1.getId());
            } else if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
                harness.handleCardChosen(player1, 0);
            } else if (!gd.interaction.isAwaitingInput()) {
                harness.passBothPriorities();
            } else {
                throw new IllegalStateException("Unexpected enter choice: " + gd.interaction.activeInteraction());
            }
        }
    }

    private void prepareCast(com.github.laxika.magicalvibes.model.Card discardedCard,
                             com.github.laxika.magicalvibes.model.Card drawnCard) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CelebrateTheMountainKing(), discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
