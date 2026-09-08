package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatalGrudge.class, GrizzlyBears.class, Millstone.class, Ornithopter.class, Forest.class})
class FatalGrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices one matching opposing permanent and draws a card")
    void sacrificesMatchingPermanentAndDraws() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent matching = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Card drawnCard = new Forest();

        harness.setLibrary(player1, List.of(drawnCard));
        cast(sacrificed);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(matching)
                .contains(nonmatching);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Opponent chooses among permanents sharing a card type with the sacrificed permanent")
    void opponentChoosesMatchingPermanent() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstMatching = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondMatching = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player2, new Millstone());

        cast(sacrificed);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(firstMatching.getId(), secondMatching.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, List.of(firstMatching.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstMatching)
                .contains(secondMatching, nonmatching);
    }

    @Test
    @DisplayName("Cannot sacrifice a land as the additional cost")
    void cannotSacrificeLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FatalGrudge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    private void cast(Permanent sacrificed) {
        harness.setHand(player1, List.of(new FatalGrudge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();
    }
}
