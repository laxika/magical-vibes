package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scaretiller.class, Forest.class, SakuraTribeElder.class})
class ScaretillerTest extends BaseCardTest {

    private static final String HAND_MODE =
            "You may put a land card from your hand onto the battlefield tapped";
    private static final String GRAVEYARD_MODE =
            "Return target land card from your graveyard to the battlefield tapped";

    @Test
    void tappedTriggerPutsAHandLandOntoTheBattlefieldTapped() {
        Permanent scaretiller = harness.addToBattlefieldAndReturn(player1, new Scaretiller());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));

        tap(scaretiller);
        resolveTriggerAndChooseMode(HAND_MODE);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredForest = findPermanent(player1, "Forest");
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void tappedTriggerTargetsAndReturnsALandFromTheGraveyardTapped() {
        Forest invalidCard = new Forest();
        SakuraTribeElder invalidCreature = new SakuraTribeElder();
        harness.setGraveyard(player1, List.of(invalidCreature, invalidCard));
        Permanent scaretiller = harness.addToBattlefieldAndReturn(player1, new Scaretiller());

        tap(scaretiller);
        resolveTriggerAndChooseMode(GRAVEYARD_MODE);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(invalidCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(invalidCard.getId()));
        harness.passBothPriorities();

        Permanent enteredForest = findPermanent(player1, "Forest");
        assertThat(enteredForest.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(invalidCreature.getId());
    }

    @Test
    void tappingAnotherPermanentDoesNotTriggerScaretiller() {
        harness.addToBattlefield(player1, new Scaretiller());
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());

        tap(elder);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingRequiresModeAndTargetBeforeAnyoneCanRespond() {
        Permanent scaretiller = harness.addToBattlefieldAndReturn(player1, new Scaretiller());
        scaretiller.setSummoningSick(false);
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(scaretiller.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, GRAVEYARD_MODE);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void handModeCanBeDeclined() {
        Permanent scaretiller = harness.addToBattlefieldAndReturn(player1, new Scaretiller());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));

        tap(scaretiller);
        resolveTriggerAndChooseMode(HAND_MODE);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardModeDoesNotReplaceATargetThatLeavesBeforeResolution() {
        Permanent scaretiller = harness.addToBattlefieldAndReturn(player1, new Scaretiller());
        Forest target = new Forest();
        Forest otherLand = new Forest();
        Forest opponentsLand = new Forest();
        harness.setGraveyard(player1, List.of(target, otherLand));
        harness.setGraveyard(player2, List.of(opponentsLand));

        tap(scaretiller);
        resolveTriggerAndChooseMode(GRAVEYARD_MODE);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(target.getId(), otherLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherLand));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveTriggerAndChooseMode(String mode) {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleListChoice(player1, mode);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
