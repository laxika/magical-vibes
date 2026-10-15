package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CracklingPerimeter.class, Forest.class, RakdosGuildgate.class,
        GruulGuildgate.class, Naturalize.class})
class CracklingPerimeterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Gate deals 1 damage to the opponent")
    void tapGateDealsDamage() {
        harness.addToBattlefield(player1, new CracklingPerimeter());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gate.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate without an untapped Gate")
    void requiresUntappedGate() {
        harness.addToBattlefield(player1, new CracklingPerimeter());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gate.tap();
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A Gate an opponent controls cannot pay the cost")
    void opponentGateDoesNotPay() {
        harness.addToBattlefield(player1, new CracklingPerimeter());
        Permanent gate = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());
        gate.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gate.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The Gate is tapped as a cost before damage resolves and cannot be reused")
    void gateIsTappedBeforeResolution() {
        harness.addToBattlefield(player1, new CracklingPerimeter());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gate.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each activation taps only the chosen Gate and multiple Gates allow repeated activation")
    void choosesOneGatePerActivation() {
        Permanent perimeter = harness.addToBattlefieldAndReturn(player1, new CracklingPerimeter());
        Permanent firstGate = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());
        Permanent secondGate = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondGate.getId());

        assertThat(firstGate.isTapped()).isFalse();
        assertThat(secondGate.isTapped()).isTrue();
        assertThat(perimeter.isTapped()).isFalse();
        harness.assertLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(firstGate.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroying Crackling Perimeter in response does not stop its activated ability")
    void abilityResolvesAfterSourceIsDestroyed() {
        Permanent perimeter = harness.addToBattlefieldAndReturn(player1, new CracklingPerimeter());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new GruulGuildgate());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, perimeter.getId());

        harness.assertNotOnBattlefield(player1, "Crackling Perimeter");
        harness.assertInGraveyard(player1, "Crackling Perimeter");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gate.isTapped()).isTrue();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
