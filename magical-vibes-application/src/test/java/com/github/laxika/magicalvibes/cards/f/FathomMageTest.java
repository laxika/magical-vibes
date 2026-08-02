package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FathomMageTest extends BaseCardTest {

    private Permanent addFathomMage(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FathomMage());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerDecks.get(activePlayer.getId()).clear();
        gd.playerDecks.get(activePlayer.getId()).add(new Forest());
    }

    private void castBearsAndResolveTriggers() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Grizzly Bears resolves, evolve triggers
        harness.passBothPriorities(); // evolve resolves, putting a +1/+1 counter
        harness.passBothPriorities(); // the +1/+1 counter trigger resolves
    }

    @Test
    @DisplayName("Evolve counter triggers an optional draw that can be accepted")
    void evolveCounterAllowsDraw() {
        Permanent mage = addFathomMage(player1);
        setUpMainPhase(player1);

        castBearsAndResolveTriggers();

        assertThat(mage.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the optional draw leaves the hand untouched")
    void decliningDrawKeepsHand() {
        addFathomMage(player1);
        setUpMainPhase(player1);

        castBearsAndResolveTriggers();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A creature that does not exceed its stats gives no counter and no draw")
    void noEvolveNoDraw() {
        Permanent mage = addFathomMage(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new FathomMage()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
