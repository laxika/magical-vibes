package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EbondeathDracolich.class, GrizzlyBears.class, Shock.class})
class EbondeathDracolichTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be cast from the graveyard when no creature died this turn")
    void cannotBeCastWithoutCreatureDeath() {
        harness.setGraveyard(player1, List.of(new EbondeathDracolich()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("A creature with the same name dying does not enable the graveyard cast")
    void sameNameCreatureDeathDoesNotEnableGraveyardCast() {
        harness.addToBattlefield(player2, new EbondeathDracolich());
        harness.setGraveyard(player1, List.of(new EbondeathDracolich()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Ebondeath, Dracolich"));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("A different creature dying enables the graveyard cast and it enters tapped")
    void differentCreatureDeathEnablesGraveyardCastAndEntersTapped() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EbondeathDracolich()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent ebondeath = findPermanent(player1, "Ebondeath, Dracolich");
        assertThat(ebondeath.isTapped()).isTrue();
    }
}
