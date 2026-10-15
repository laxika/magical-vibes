package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FortunateFew.class, DarksteelRelic.class, GrizzlyBears.class, Millstone.class, Forest.class})
class FortunateFewTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesDistinctOpponentNonlandAndAllOtherNonlandsAreDestroyed() {
        Permanent player1Bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Millstone = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Relic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        cast();

        PendingInteraction.MultiPermanentChoice player1Choice = activeChoice();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.validIds()).containsExactly(player2Bear.getId(), player2Relic.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Exactly one permanent");

        harness.handleMultiplePermanentsChosen(player1, List.of(player2Relic.getId()));

        PendingInteraction.MultiPermanentChoice player2Choice = activeChoice();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.validIds()).containsExactly(player1Bear.getId(), player1Millstone.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player1Bear.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(player1Bear, player1Forest)
                .doesNotContain(player1Millstone);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(player2Relic, player2Forest)
                .doesNotContain(player2Bear);
    }

    private PendingInteraction.MultiPermanentChoice activeChoice() {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        return choice;
    }

    private void cast() {
        harness.setHand(player1, List.of(new FortunateFew()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
