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

    @Test
    @DisplayName("An empty controller graveyard does not prevent an opponent from accepting")
    void emptyControllerGraveyardStillAllowsOpponentReturn() {
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentCreature));

        castTemptWithImmortality();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentCreature.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("An opponent without creatures cannot grant an additional return")
    void opponentWithoutCreaturesDoesNotGrantReward() {
        Card ownFirst = new GrizzlyBears();
        Card ownReward = new GrizzlyBears();
        Card noncreature = new TemptWithImmortality();
        harness.setGraveyard(player1, List.of(ownFirst, ownReward));
        harness.setGraveyard(player2, List.of(noncreature));

        castTemptWithImmortality();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownFirst.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownReward);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(noncreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Creature choices skip noncreature cards in both graveyards")
    void returnsOnlyCreaturesFromMixedGraveyards() {
        Card ownNoncreature = new TemptWithImmortality();
        Card opponentNoncreature = new TemptWithImmortality();
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownNoncreature, ownCreature));
        harness.setGraveyard(player2, List.of(opponentNoncreature, opponentCreature));

        castTemptWithImmortality();
        harness.handleGraveyardCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(ownCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownNoncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentNoncreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }
    private void castTemptWithImmortality() {
        harness.setHand(player1, List.of(new TemptWithImmortality()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
