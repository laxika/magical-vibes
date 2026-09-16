package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistOfStagnation.class, RiftstonePortal.class, SuntailHawk.class})
class MistOfStagnationTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents permanents from untapping during their controllers' untap steps")
    void preventsUntapDuringUntapSteps() {
        addPermanent(player1, new MistOfStagnation());
        Permanent ownLand = addReady(player1, new RiftstonePortal());
        Permanent opponentCreature = addCreatureReady(player2, new SuntailHawk());
        ownLand.tap();
        opponentCreature.tap();

        advanceToUpkeep(player1);

        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The active player chooses distinct permanents using their graveyard count")
    void choosesDistinctPermanentsForActivePlayersGraveyard() {
        addPermanent(player1, new MistOfStagnation());
        Permanent ownLand = addReady(player1, new RiftstonePortal());
        Permanent opponentCreature = addCreatureReady(player2, new SuntailHawk());
        ownLand.tap();
        opponentCreature.tap();

        harness.setGraveyard(player1, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));
        harness.setGraveyard(player2, List.of(new SuntailHawk()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                gd.playerBattlefields.get(player1.getId()).get(0).getId(),
                ownLand.getId(), opponentCreature.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentCreature.getId()));

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps all available permanents when the graveyard count is larger")
    void untapsAllAvailablePermanentsWhenCountIsLarger() {
        Permanent mist = addReady(player1, new MistOfStagnation());
        Permanent ownLand = addReady(player1, new RiftstonePortal());
        mist.tap();
        ownLand.tap();
        harness.setGraveyard(player2, List.of(
                new SuntailHawk(), new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(mist.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not ask for a choice when the active player's graveyard is empty")
    void doesNotAskForChoiceWithEmptyActivePlayersGraveyard() {
        addPermanent(player1, new MistOfStagnation());
        Permanent opponentCreature = addCreatureReady(player2, new SuntailHawk());
        opponentCreature.tap();
        harness.setGraveyard(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires distinct permanents when choosing more than one permanent")
    void choosesMultipleDistinctPermanents() {
        addPermanent(player1, new MistOfStagnation());
        Permanent ownLand = addReady(player1, new RiftstonePortal());
        Permanent ownCreature = addCreatureReady(player1, new SuntailHawk());
        Permanent opponentCreature = addCreatureReady(player2, new SuntailHawk());
        ownLand.tap();
        ownCreature.tap();
        opponentCreature.tap();
        harness.setGraveyard(player2, List.of(new SuntailHawk(), new SuntailHawk()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player2, List.of(ownLand.getId(), ownLand.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player2, List.of(ownLand.getId(), opponentCreature.getId()));

        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(ownCreature.isTapped()).isTrue();
    }

    private Permanent addPermanent(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = addPermanent(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
