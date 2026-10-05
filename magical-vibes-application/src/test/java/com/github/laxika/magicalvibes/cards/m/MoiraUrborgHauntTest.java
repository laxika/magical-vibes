package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MoiraUrborgHaunt.class, GrizzlyBears.class, DoomBlade.class})
class MoiraUrborgHauntTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a creature that died from the battlefield this turn")
    void combatDamageReturnsEligibleCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getCard().getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cards not put into the graveyard from the battlefield this turn are not eligible")
    void ignoresCardsNotPutThereFromBattlefieldThisTurn() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exactly one eligible creature must be targeted and returned")
    void returnsOnlyTheSelectedCreature() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.castAndResolveInstant(player1, 0, second.getId());

        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        resolveCombat();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getCard().getId(), second.getCard().getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getCard().getId(), second.getCard().getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getCard().getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .extracting(p -> p.getCard().getId()).containsExactly(second.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(first.getCard().getId())
                .doesNotContain(second.getCard().getId());
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Grizzly Bears").isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A creature that died this turn in an opponent's graveyard is not eligible")
    void ignoresOpponentGraveyard() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that died on a previous turn is not eligible")
    void ignoresCreatureThatDiedOnPreviousTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void rejectsSingleBlocker() {
        Permanent moira = addCreatureReady(player1, new MoiraUrborgHaunt());
        moira.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }
}
