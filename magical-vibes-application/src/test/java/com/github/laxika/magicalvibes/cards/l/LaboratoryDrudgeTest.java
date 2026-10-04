package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaboratoryDrudge.class, AncientGrudge.class, PrismaticLens.class, ReassemblingSkeleton.class})
class LaboratoryDrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws at the end step after casting a spell from a graveyard")
    void drawsAfterCastingSpellFromGraveyard() {
        addDrudge();
        Permanent lens = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        harness.setGraveyard(player1, List.of(new AncientGrudge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFlashback(player1, 0, lens.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Draws at the end step after activating a graveyard card ability")
    void drawsAfterActivatingGraveyardCardAbility() {
        addDrudge();
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        harness.assertOnBattlefield(player1, "Reassembling Skeleton");
    }

    @Test
    @DisplayName("Does not draw at the end step without a qualifying action")
    void doesNotDrawWithoutQualifyingAction() {
        addDrudge();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    private Permanent addDrudge() {
        return addCreatureReady(player1, new LaboratoryDrudge());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
