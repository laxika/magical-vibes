package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhessianLookout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OfOneMind.class, JhessianLookout.class, GrizzlyBears.class})
class OfOneMindTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {U} when you control a Human creature and a non-Human creature")
    void costsOneBlueWithHumanAndNonHumanCreature() {
        harness.addToBattlefield(player1, new JhessianLookout());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a Human creature")
    void doesNotGetReductionWithOnlyNonHumanCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a non-Human creature")
    void doesNotGetReductionWithOnlyHumanCreature() {
        harness.addToBattlefield(player1, new JhessianLookout());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Of One Mind draws two cards")
    void resolvingDrawsTwoCards() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.addToBattlefield(player1, new JhessianLookout());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }
}
