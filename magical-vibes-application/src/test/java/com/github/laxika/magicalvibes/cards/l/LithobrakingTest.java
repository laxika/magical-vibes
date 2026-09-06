package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EntropicBattlecruiser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lithobraking.class, EntropicBattlecruiser.class, GrizzlyBears.class})
class LithobrakingTest extends BaseCardTest {

    @Test
    void createsLanderAndSacrificingAnArtifactDealsDamageToEachCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLithobraking();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Entropic Battlecruiser");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void decliningArtifactSacrificeLeavesCreaturesAndCreatesLander() {
        harness.addToBattlefield(player1, new EntropicBattlecruiser());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castLithobraking();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Entropic Battlecruiser");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    private void castLithobraking() {
        harness.setHand(player1, java.util.List.of(new Lithobraking()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
    }
}
