package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DromokaTheEternal;
import com.github.laxika.magicalvibes.cards.f.FrontierSiege;
import com.github.laxika.magicalvibes.cards.g.GhostfireBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UginsConstruct.class, DromokaTheEternal.class, FrontierSiege.class,
        GhostfireBlade.class, GrizzlyBears.class})
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
        Permanent multicolored = harness.addToBattlefieldAndReturn(player1, new DromokaTheEternal());
        Permanent colored = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GhostfireBlade());

        castUginsConstruct();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(multicolored.getId(), colored.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(multicolored.getId()));

        harness.assertInGraveyard(player1, "Dromoka, the Eternal");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ghostfire Blade");
        harness.assertOnBattlefield(player1, "Ugin's Construct");
    }

    @Test
    @DisplayName("ETB does nothing when the controller has only colorless permanents")
    void noColoredPermanents() {
        harness.addToBattlefield(player1, new UginsConstruct());

        castUginsConstruct();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Ugin's Construct");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot sacrifice an opponent's colored permanent")
    void opponentColoredPermanentIsNotEligible() {
        harness.addToBattlefield(player2, new DromokaTheEternal());

        castUginsConstruct();

        harness.assertOnBattlefield(player2, "Dromoka, the Eternal");
        harness.assertOnBattlefield(player1, "Ugin's Construct");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB sacrifices a colored enchantment when it is the only eligible permanent")
    void sacrificesColoredNoncreature() {
        harness.addToBattlefield(player1, new FrontierSiege());

        castUginsConstruct();

        harness.assertInGraveyard(player1, "Frontier Siege");
        harness.assertOnBattlefield(player1, "Ugin's Construct");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB sacrifices only the controller's colored permanent")
    void onlyControllerSacrifices() {
        harness.addToBattlefield(player1, new FrontierSiege());
        harness.addToBattlefield(player2, new DromokaTheEternal());

        castUginsConstruct();

        harness.assertInGraveyard(player1, "Frontier Siege");
        harness.assertOnBattlefield(player2, "Dromoka, the Eternal");
        harness.assertOnBattlefield(player1, "Ugin's Construct");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castUginsConstruct() {
        harness.setHand(player1, List.of(new UginsConstruct()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
