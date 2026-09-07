package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MistOfStagnation.class, Forest.class, GrizzlyBears.class, Shock.class})
class MistOfStagnationTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents permanents from untapping during their controllers' untap steps")
    void preventsUntapDuringUntapSteps() {
        addPermanent(player1, new MistOfStagnation());
        Permanent ownLand = addReady(player1, new Forest());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());
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
        Permanent ownLand = addReady(player1, new Forest());
        Permanent opponentCreature = addReady(player2, new GrizzlyBears());
        ownLand.tap();
        opponentCreature.tap();

        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setGraveyard(player2, List.of(new Shock()));

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
        Permanent ownLand = addReady(player1, new Forest());
        mist.tap();
        ownLand.tap();
        harness.setGraveyard(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(mist.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isFalse();
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
