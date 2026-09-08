package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.SoldeviMachinist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OupheVandalsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact activated ability, destroys its source, and sacrifices itself")
    void countersArtifactActivatedAbilityAndDestroysSource() {
        harness.addToBattlefield(player1, new OupheVandals());
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, rod.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player1, "Ouphe Vandals");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotTargetManaAbility() {
        harness.addToBattlefield(player1, new OupheVandals());
        SoldeviMachinist machinist = new SoldeviMachinist();
        harness.addToBattlefieldAndReturn(player2, machinist).setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, machinist.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a non-artifact source")
    void cannotTargetNonArtifactAbility() {
        harness.addToBattlefield(player1, new OupheVandals());
        harness.addToBattlefield(player1, new GrizzlyBears());
        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player2, fumeSpitter);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fumeSpitter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
