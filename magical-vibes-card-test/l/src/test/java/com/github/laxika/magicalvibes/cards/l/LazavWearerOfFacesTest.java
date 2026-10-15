package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LazavWearerOfFaces.class, GrizzlyBears.class, HillGiant.class})
class LazavWearerOfFacesTest extends BaseCardTest {

    @Test
    void attacksExileTargetGraveyardCardAndInvestigate() {
        Permanent lazav = addReadyLazav();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
        assertThat(gd.getCardsExiledByPermanent(lazav.getId())).containsExactly(bears);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificingClueMayCopyOneCreatureExiledWithLazavUntilEndOfTurn() {
        Permanent lazav = addReadyLazav();
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        harness.setGraveyard(player2, List.of(bears));

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();
        gd.addToExile(player2.getId(), giant, lazav.getId());

        Permanent clue = findPermanents(player1, "Clue").getFirst();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ExiledCreatureCopyChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCreatureCopyChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), giant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));

        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lazav)).isEqualTo(3);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void illegalGraveyardTargetPreventsInvestigating() {
        addReadyLazav();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player2, List.of());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void mayDeclineCopyAfterSacrificingClue() {
        Permanent lazav = addReadyLazav();
        Card giant = new HillGiant();
        harness.setGraveyard(player2, List.of(giant));

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        resolveAllTriggers();

        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lazav)).isEqualTo(3);
        assertThat(gd.getCardsExiledByPermanent(lazav.getId())).containsExactly(giant);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void copyRevertsAtEndOfTurnAndKeepsExiledCardsLinked() {
        Permanent lazav = addReadyLazav();
        Card giant = new HillGiant();
        harness.setGraveyard(player2, List.of(giant));

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        resolveAllTriggers();

        Permanent clue = findPermanent(player1, "Clue");
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(3);
        resolveAllTriggers();
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, lazav)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lazav)).isEqualTo(3);
        assertThat(gd.getCardsExiledByPermanent(lazav.getId())).containsExactly(giant);
    }
    private Permanent addReadyLazav() {
        return addCreatureReady(player1, new LazavWearerOfFaces());
    }
}
