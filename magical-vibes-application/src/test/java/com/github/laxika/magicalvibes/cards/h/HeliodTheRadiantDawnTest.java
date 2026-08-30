package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cessation;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.ErebosGodOfTheDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        HeliodTheRadiantDawn.class,
        HeliodTheWarpedEclipse.class,
        Cessation.class,
        ErebosGodOfTheDead.class,
        Divination.class
})
class HeliodTheRadiantDawnTest extends BaseCardTest {

    @Test
    void returnsTargetNonGodEnchantmentFromGraveyardToHand() {
        Card cessation = new Cessation();
        Card god = new ErebosGodOfTheDead();
        harness.setGraveyard(player1, List.of(cessation, god));
        harness.setHand(player1, List.of(new HeliodTheRadiantDawn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(cessation.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cessation);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(god);
    }

    @Test
    void transformAbilityUsesSorceryTiming() {
        Permanent heliod = addHeliod();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(heliod.isTransformed()).isTrue();
    }

    @Test
    void warpedEclipseGrantsFlashAndReducesCostsByCardsOpponentsDrew() {
        addTransformedHeliod();
        gd.cardsDrawnThisTurn.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void warpedEclipseLetsControllerCastSorceriesDuringOpponentTurn() {
        addTransformedHeliod();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addHeliod() {
        harness.addToBattlefield(player1, new HeliodTheRadiantDawn());
        Permanent heliod = gd.playerBattlefields.get(player1.getId()).getFirst();
        heliod.setSummoningSick(false);
        return heliod;
    }

    private Permanent addTransformedHeliod() {
        Permanent heliod = addHeliod();
        heliod.setCard(heliod.getCard().getBackFaceCard());
        heliod.setTransformed(true);
        return heliod;
    }
}
