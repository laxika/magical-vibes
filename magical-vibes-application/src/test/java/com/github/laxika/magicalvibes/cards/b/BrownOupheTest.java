package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoldeviMachinist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrownOupheTest extends BaseCardTest {

    /** The Ouphe's ability needs {T}, so it must have been under its controller's control since their turn began. */
    private void addReadyOuphe(Player player) {
        harness.addToBattlefieldAndReturn(player, new BrownOuphe()).setSummoningSick(false);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Counters an activated ability from an artifact source")
    void countersArtifactActivatedAbility() {
        addReadyOuphe(player1);

        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, rod.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotCounterManaAbility() {
        addReadyOuphe(player1);

        SoldeviMachinist machinist = new SoldeviMachinist();
        harness.addToBattlefieldAndReturn(player2, machinist).setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, machinist.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a non-artifact source")
    void cannotCounterNonArtifactAbility() {
        addReadyOuphe(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        UUID fumeSpitterId = fumeSpitter.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fumeSpitterId))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotCounterSpell() {
        addReadyOuphe(player1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shockId))
                .isInstanceOf(IllegalStateException.class);
    }
}
