package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProsperTomeBound.class, Forest.class, GrizzlyBears.class})
class ProsperTomeBoundTest extends BaseCardTest {

    @Test
    void exilesTopCardAtEndStepWithPermissionUntilEndOfNextTurn() {
        addProsper();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        goToEndStepAndResolve();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(topCard.getId());
    }

    @Test
    void castingExiledSpellCreatesTreasure() {
        addProsper();
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    void playingExiledLandCreatesTreasure() {
        addProsper();
        Forest land = new Forest();
        gd.addToExile(player1.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player1.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, land.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    void castingFromHandDoesNotCreateTreasure() {
        addProsper();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }

    private Permanent addProsper() {
        return addCreatureReady(player1, new ProsperTomeBound());
    }

    private void goToEndStepAndResolve() {
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
