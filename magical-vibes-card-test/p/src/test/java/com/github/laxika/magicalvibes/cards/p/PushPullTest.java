package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoercedToKill;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PushPull.class, GrizzlyBears.class, CoercedToKill.class})
class PushPullTest extends BaseCardTest {

    private static final int PUSH = 0;
    private static final int PULL = 1;

    @Test
    @DisplayName("Push destroys a tapped creature")
    void pushDestroysTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        harness.setHand(player1, List.of(new PushPull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, PUSH, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pull returns creatures with haste and sacrifices them at the next end step")
    void pullReturnsCreaturesWithHasteUntilNextEndStep() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        castPull();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        List<Permanent> returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == first || permanent.getCard() == second)
                .toList();
        assertThat(returned).hasSize(2);
        assertThat(returned).allMatch(permanent -> permanent.getGrantedKeywords().contains(Keyword.HASTE));

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == first || permanent.getCard() == second);
    }

    @Test
    @DisplayName("Pull requires all selected cards to come from one graveyard")
    void pullRequiresSingleGraveyard() {
        Card ownCard = new GrizzlyBears();
        Card opponentFirst = new GrizzlyBears();
        Card opponentSecond = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentFirst, opponentSecond));
        castPull();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(ownCard.getId(), opponentFirst.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");

        harness.handleMultipleCardsChosen(player1, List.of(opponentFirst.getId(), opponentSecond.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == opponentFirst || permanent.getCard() == opponentSecond))
                .hasSize(2);
    }

    private void castPull() {
        harness.setHand(player1, List.of(new PushPull()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, PULL, List.of());
    }

    @Test
    void pullReturnsRemainingLegalTarget() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        castPull();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player2, List.of(second));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getGrantedKeywords())
                .contains(Keyword.HASTE);
    }

    @Test
    void pullMayChooseNoTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        castPull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void pullCanResolveWithEmptyGraveyards() {
        castPull();
        if (gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class) != null) {
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Push // Pull");
    }

    @Test
    void pullCannotChooseNoncreatureCard() {
        Card creature = new GrizzlyBears();
        Card sorcery = new PushPull();
        harness.setGraveyard(player1, List.of(creature, sorcery));
        castPull();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sorcery);
    }

    @Test
    void pushCannotTargetUntappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PushPull()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, PUSH, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void pushDoesNotDestroyCreatureThatUntapsBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new PushPull()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalSorcery(player1, 0, PUSH, List.of(creature.getId()));
        creature.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void pullCreatesOneSacrificeTriggerForBothCreatures() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        castPull();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void pullCannotSacrificeCreatureNowControlledByOpponent() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castPull();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CoercedToKill()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castEnchantment(player2, 0, returned.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
