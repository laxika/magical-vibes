package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptWithImmortality.class, GrizzlyBears.class})
class TemptWithImmortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature for the controller and an accepting opponent, then rewards the controller")
    void acceptingOpponentReturnsCreaturesForBothPlayersAndRewardsController() {
        Card ownFirst = new GrizzlyBears();
        Card ownReward = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownFirst, ownReward));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castTemptWithImmortality();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(ownFirst.getId(), ownReward.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant an additional creature")
    void decliningOpponentDoesNotReturnAnAdditionalCreature() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castTemptWithImmortality();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
    }

    private void castTemptWithImmortality() {
        harness.setHand(player1, List.of(new TemptWithImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
