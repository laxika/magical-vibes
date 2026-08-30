package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoldeviMachinist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rust.class, RodOfRuin.class, SoldeviMachinist.class, FumeSpitter.class, GrizzlyBears.class, Shock.class})
class RustTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an activated ability from an artifact source")
    void countersArtifactActivatedAbility() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, rod.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotCounterManaAbility() {
        SoldeviMachinist machinist = new SoldeviMachinist();
        harness.addToBattlefieldAndReturn(player2, machinist).setSummoningSick(false);

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, machinist.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a non-artifact source")
    void cannotCounterNonArtifactAbility() {
        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fumeSpitter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotCounterSpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new Rust()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
