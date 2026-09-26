package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdvancedReconstruction.class, Disentomb.class, GrizzlyBears.class})
class AdvancedReconstructionTest extends BaseCardTest {

    @Test
    void firstMainPhaseMillsThenExilesTheMilledCardWithPlayPermission() {
        harness.addToBattlefield(player1, new AdvancedReconstruction());
        Card milled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milled));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(milled);
        assertThat(gd.exilePlayPermissions).containsEntry(milled.getId(), player1.getId());
    }

    @Test
    void levelTwoDealsDamageWhenCardsLeaveTheControllersGraveyard() {
        Permanent reconstruction = harness.addToBattlefieldAndReturn(player1, new AdvancedReconstruction());
        levelUpToTwo(reconstruction);

        harness.setLife(player2, 20);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void levelThreeReducesNonHandSpellCostsButNotHandSpellCosts() {
        Permanent reconstruction = harness.addToBattlefieldAndReturn(player1, new AdvancedReconstruction());
        levelUpToThree(reconstruction);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        gd.graveyardPlayPermissions.put(creature.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void levelUpToTwo(Permanent reconstruction) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(reconstruction), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent reconstruction) {
        levelUpToTwo(reconstruction);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(reconstruction), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
