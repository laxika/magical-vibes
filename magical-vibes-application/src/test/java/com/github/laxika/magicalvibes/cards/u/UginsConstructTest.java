package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AbzanKinGuard;
import com.github.laxika.magicalvibes.cards.g.GhostfireBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UginsConstructTest extends BaseCardTest {

    @Test
    @DisplayName("ETB sacrifices a colored permanent and leaves a colorless permanent alone")
    void sacrificesColoredPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GhostfireBlade());

        castUginsConstruct();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ghostfire Blade");
        harness.assertOnBattlefield(player1, "Ugin's Construct");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB can sacrifice a multicolored permanent")
    void sacrificesMulticoloredPermanent() {
        Permanent multicolored = harness.addToBattlefieldAndReturn(player1, new AbzanKinGuard());
        Permanent colored = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GhostfireBlade());

        castUginsConstruct();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(multicolored.getId(), colored.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(multicolored.getId()));

        harness.assertInGraveyard(player1, "Abzan Kin-Guard");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ghostfire Blade");
        harness.assertOnBattlefield(player1, "Ugin's Construct");
    }

    private void castUginsConstruct() {
        harness.setHand(player1, List.of(new UginsConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
