package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerrorOfThePeaksTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering deals damage equal to its power to any target")
    void anotherCreatureDealsItsPowerToAnyTarget() {
        harness.addToBattlefield(player1, new TerrorOfThePeaks());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The entering creature's power is evaluated when the trigger resolves")
    void enteringPowerIsEvaluatedOnResolution() {
        harness.addToBattlefield(player1, new TerrorOfThePeaks());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        UUID enteringId = harness.getPermanentId(player1, "Hill Giant");
        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(enteringId))
                .findFirst()
                .orElseThrow();
        entering.setPowerModifier(2);

        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The entering creature's last known power is used if it leaves before resolution")
    void usesEnteringCreatureLastKnownPower() {
        harness.addToBattlefield(player1, new TerrorOfThePeaks());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        UUID enteringId = harness.getPermanentId(player1, "Hill Giant");
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(enteringId));

        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An opponent pays 3 life when targeting Terror of the Peaks")
    void opponentPaysLifeWhenTargetingTerror() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new TerrorOfThePeaks());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castInstant(player2, 0, terror.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Terror of the Peaks does not tax a spell targeting another permanent")
    void spellTargetingAnotherPermanentIsNotTaxed() {
        harness.addToBattlefield(player1, new TerrorOfThePeaks());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent with insufficient life cannot target Terror of the Peaks")
    void opponentNeedsEnoughLifeToTargetTerror() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new TerrorOfThePeaks());
        harness.setLife(player2, 2);
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, terror.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("targeting life cost");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gameData = harness.getGameData();
            if (gameData.interaction.isAwaitingInput() || gameData.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private void prepareOpponentCast(LightningBolt spell, ManaColor color, int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, amount);
    }
}
