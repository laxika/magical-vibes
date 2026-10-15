package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Ebondeath, Dracolich"));

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

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent ebondeath = findPermanent(player1, "Ebondeath, Dracolich");
        assertThat(ebondeath.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can flash in from hand without a death and enters tapped")
    void handCastDoesNotRequireCreatureDeath() {
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.castFromHand(player1, new EbondeathDracolich(), "{2}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ebondeath, Dracolich").isTapped()).isTrue();
    }

    @Test
    @DisplayName("An own creature dying before Ebondeath reaches the graveyard enables flash casting in response")
    void canCastFromGraveyardInResponseDuringOpponentsTurn() {
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.setGraveyard(player1, List.of(new EbondeathDracolich()));
        harness.castInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ebondeath, Dracolich").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Ebondeath, Dracolich");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A death on the previous turn does not enable casting from the graveyard")
    void graveyardPermissionExpiresAtTurnBoundary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EbondeathDracolich()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Graveyard casting still requires paying the mana cost")
    void graveyardCastRequiresMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EbondeathDracolich()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Ebondeath, Dracolich");
        harness.assertNotOnBattlefield(player1, "Ebondeath, Dracolich");
    }
}
