package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfLoss.class, GrizzlyBears.class, Plains.class})
class SeasonOfLossTest extends BaseCardTest {

    @Test
    @DisplayName("Can choose no modes")
    void canChooseNoModes() {
        cast(modeIndex(0, 0, 0));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player sacrifices a creature")
    void eachPlayerSacrificesCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(modeIndex(1, 0, 0));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The sacrifice mode can be chosen twice")
    void sacrificeModeCanBeChosenTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(modeIndex(2, 0, 0));

        for (int i = 0; i < 2; i++) {
            PendingInteraction.MultiPermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
            assertThat(choice).isNotNull();
            var choosingPlayer = choice.playerId().equals(player1.getId()) ? player1 : player2;
            harness.handleMultiplePermanentsChosen(choosingPlayer, List.of(choice.validIds().getFirst()));
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws for each creature that died under your control")
    void drawsForCreaturesThatDiedUnderYourControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));

        cast(modeIndex(1, 1, 0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each opponent loses life for creature cards in your graveyard")
    void eachOpponentLosesLifeForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains()));

        cast(modeIndex(0, 0, 1));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The sacrifice mode resolves before the life-loss mode")
    void sacrificeResolvesBeforeLifeLoss() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(modeIndex(1, 0, 1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new SeasonOfLoss()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, modeIndex);
        harness.passBothPriorities();
    }

    private int modeIndex(int sacrifices, int draws, int lifeLosses) {
        int index = 0;
        for (int currentSacrifices = 0; currentSacrifices <= 5; currentSacrifices++) {
            for (int currentDraws = 0; currentDraws <= (5 - currentSacrifices) / 2; currentDraws++) {
                for (int currentLifeLosses = 0;
                        currentLifeLosses <= (5 - currentSacrifices - 2 * currentDraws) / 3;
                        currentLifeLosses++) {
                    if (currentSacrifices == sacrifices && currentDraws == draws
                            && currentLifeLosses == lifeLosses) {
                        return index;
                    }
                    index++;
                }
            }
        }
        throw new IllegalArgumentException("Invalid Season of Loss mode combination");
    }
}
