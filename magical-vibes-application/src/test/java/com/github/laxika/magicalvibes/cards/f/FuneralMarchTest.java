package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuneralMarch.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class})
class FuneralMarchTest extends BaseCardTest {
    @Test
    void canEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuneralMarch()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> creature.getId().equals(permanent.getAttachedTo())
                        && permanent.getCard() instanceof FuneralMarch);
    }

    @Test
    void controllerChoosesCreatureToSacrifice() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstVictim = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent secondVictim = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attachFuneralMarch(player1, enchanted);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, secondVictim.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondVictim.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstVictim)
                .doesNotContain(secondVictim);
    }

    @Test
    void controllerSacrificesWhenEnchantedCreatureReturnsToHand() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attachFuneralMarch(player1, enchanted);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, enchanted));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(enchanted.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(victim.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(victim);
    }

    private Permanent attachFuneralMarch(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FuneralMarch());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    @Test
    @DisplayName("When the enchanted creature leaves, its controller sacrifices a creature of their choice")
    void controllerSacrificesWhenEnchantedCreatureLeaves() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attachFuneralMarch(player1, enchanted);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted));
        resolveAllTriggers();

        // Only the victim remained after the enchanted creature died, so it is auto-sacrificed.
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The enchanted creature's controller sacrifices, even when a different player controls the Aura")
    void enchantedControllerSacrificesNotAuraController() {
        // Aura controlled by player1, but it enchants player2's creature.
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attachFuneralMarch(player1, enchanted);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, enchanted));
        resolveAllTriggers();

        // Player2 (the enchanted creature's controller) sacrifices; player1's board is untouched.
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Funeral March cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new FuneralMarch()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
