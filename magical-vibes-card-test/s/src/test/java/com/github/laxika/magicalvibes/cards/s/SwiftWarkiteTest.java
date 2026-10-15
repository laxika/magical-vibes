package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KolaghanAspirant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({SwiftWarkite.class, KolaghanAspirant.class, SummitProwler.class,
        ScreamreachBrawler.class, StormcragElemental.class, SibsigIcebreakers.class})
class SwiftWarkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an eligible creature from hand onto the battlefield with haste and returns it at the next end step")
    void putsCreatureFromHandWithHasteAndReturnsAtNextEndStep() {
        Card warkite = new SwiftWarkite();
        Card bear = new KolaghanAspirant();
        Card hillGiant = new SummitProwler();
        harness.setHand(player1, List.of(warkite, bear, hillGiant));
        castWarkite();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        Permanent entered = findPermanent(player1, "Kolaghan Aspirant");
        assertThat(entered.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(
                        entered.getId(), DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Kolaghan Aspirant");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kolaghan Aspirant");
        harness.assertOnBattlefield(player1, "Swift Warkite");
    }

    @Test
    @DisplayName("Puts an eligible creature from the graveyard onto the battlefield")
    void putsCreatureFromGraveyardOntoBattlefield() {
        Card warkite = new SwiftWarkite();
        Card bear = new KolaghanAspirant();
        harness.setHand(player1, List.of(warkite));
        harness.setGraveyard(player1, List.of(bear));
        castWarkite();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        Permanent entered = findPermanent(player1, "Kolaghan Aspirant");
        assertThat(entered.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("May decline putting a creature onto the battlefield")
    void mayDeclinePuttingCreatureOntoBattlefield() {
        Card warkite = new SwiftWarkite();
        Card bear = new KolaghanAspirant();
        harness.setHand(player1, List.of(warkite, bear));
        castWarkite();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bear.getId()));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Mana value three is eligible, but a cheaper face-down casting option is not")
    void usesManaValueRatherThanAlternateCastingCost() {
        Card brawler = new ScreamreachBrawler();
        Card elemental = new StormcragElemental();
        Card opposingCreature = new ScreamreachBrawler();
        harness.setHand(player1, List.of(new SwiftWarkite(), brawler, elemental));
        harness.setGraveyard(player2, List.of(opposingCreature));
        castWarkite();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(brawler.getId());
        harness.handleMultipleCardsChosen(player1, List.of(brawler.getId()));

        harness.assertOnBattlefield(player1, "Screamreach Brawler");
        harness.assertInHand(player1, "Stormcrag Elemental");
        harness.assertInGraveyard(player2, "Screamreach Brawler");
    }

    @Test
    @DisplayName("Putting a creature onto the battlefield triggers its enters ability")
    void returnedCreatureTriggersItsEntersAbility() {
        Card icebreakers = new SibsigIcebreakers();
        Card discard = new ScreamreachBrawler();
        harness.setHand(player1, List.of(new SwiftWarkite(), discard));
        harness.setGraveyard(player1, List.of(icebreakers));
        harness.setHand(player2, List.of());
        castWarkite();
        harness.handleMultipleCardsChosen(player1, List.of(icebreakers.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Screamreach Brawler");
        harness.assertOnBattlefield(player1, "Sibsig Icebreakers");
    }

    @Test
    @DisplayName("The enters ability completes without a choice when no eligible creature exists")
    void noEligibleCreatureNeedsNoChoice() {
        harness.setHand(player1, List.of(new SwiftWarkite(), new StormcragElemental()));
        castWarkite();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Swift Warkite");
        harness.assertInHand(player1, "Stormcrag Elemental");
    }

    private void castWarkite() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
