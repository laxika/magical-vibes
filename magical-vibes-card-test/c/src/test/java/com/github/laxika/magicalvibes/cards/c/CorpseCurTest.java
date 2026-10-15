package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.cards.b.BlightMamba;
import com.github.laxika.magicalvibes.cards.g.GoldenUrn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseCur.class, BlightMamba.class, ContagiousNim.class, AlphaTyrranax.class, GoldenUrn.class})
class CorpseCurTest extends BaseCardTest {

    private void castCorpseCur() {
        harness.castFromHand(player1, new CorpseCur(), "{4}");
        harness.passBothPriorities();
    }

    private void resolveTarget(Card target, boolean accept) {
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    @Test
    void resolvingTriggersMayPrompt() {
        Card target = new BlightMamba();
        harness.setGraveyard(player1, List.of(target));
        castCorpseCur();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void decliningMaySkipsAbility() {
        Card target = new BlightMamba();
        harness.setGraveyard(player1, List.of(target));
        castCorpseCur();
        resolveTarget(target, false);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blight Mamba");
        harness.assertNotInHand(player1, "Blight Mamba");
    }

    @Test
    void returnsInfectCreatureFromGraveyardToHand() {
        Card target = new BlightMamba();
        harness.setGraveyard(player1, List.of(target));
        castCorpseCur();
        resolveTarget(target, true);
        harness.assertInHand(player1, "Blight Mamba");
        harness.assertNotInGraveyard(player1, "Blight Mamba");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesSpecificInfectCreature() {
        Card other = new BlightMamba();
        Card target = new ContagiousNim();
        harness.setGraveyard(player1, List.of(other, target));
        castCorpseCur();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(other.getId(), target.getId());
        resolveTarget(target, true);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    void cannotReturnCreatureWithoutInfect() {
        Card invalid = new AlphaTyrranax();
        Card target = new BlightMamba();
        harness.setGraveyard(player1, List.of(invalid, target));
        castCorpseCur();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(invalid.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noEffectWithNoInfectCreaturesInGraveyard() {
        Card creature = new AlphaTyrranax();
        Card artifact = new GoldenUrn();
        harness.setGraveyard(player1, List.of(creature, artifact));
        castCorpseCur();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, artifact);
    }

    @Test
    void noEffectWithEmptyGraveyard() {
        castCorpseCur();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Corpse Cur");
    }

    @Test
    void cannotDeclineRequiredTargetSelection() {
        Card target = new BlightMamba();
        harness.setGraveyard(player1, List.of(target));
        castCorpseCur();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        resolveTarget(target, false);
        harness.assertInGraveyard(player1, "Blight Mamba");
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card opposing = new BlightMamba();
        harness.setGraveyard(player2, List.of(opposing));
        castCorpseCur();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
    }

    @Test
    void removedTargetDoesNotReturnAnotherCreature() {
        Card target = new BlightMamba();
        Card other = new ContagiousNim();
        harness.setGraveyard(player1, List.of(target, other));
        castCorpseCur();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }
}
