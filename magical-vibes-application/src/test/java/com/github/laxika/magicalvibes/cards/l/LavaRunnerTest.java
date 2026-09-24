package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavaRunner.class, Forest.class, Mountain.class, GiantGrowth.class, ProdigalPyromancer.class})
class LavaRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("The controller of a targeting spell chooses a land to sacrifice")
    void targetingSpellControllerChoosesLand() {
        Permanent lavaRunner = harness.addToBattlefieldAndReturn(player1, new LavaRunner());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, lavaRunner.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent mountain = findPermanent(player2, "Mountain");
        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Lava Runner");
    }

    @Test
    @DisplayName("The controller of a targeting ability sacrifices a land")
    void targetingAbilityControllerSacrificesLand() {
        Permanent lavaRunner = harness.addToBattlefieldAndReturn(player1, new LavaRunner());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, lavaRunner.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Lava Runner");
    }

    @Test
    @DisplayName("The controller of a targeting spell can be Lava Runner's controller")
    void targetingSpellControllerCanBeLavaRunnersController() {
        Permanent lavaRunner = harness.addToBattlefieldAndReturn(player1, new LavaRunner());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, lavaRunner.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Lava Runner");
    }

    @Test
    @DisplayName("Only lands are offered for the targeting spell's controller to sacrifice")
    void onlyOffersLandsForTargetingSpell() {
        Permanent lavaRunner = harness.addToBattlefieldAndReturn(player1, new LavaRunner());
        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, lavaRunner.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(forest.getId(), mountain.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));

        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("No land is sacrificed when the targeting spell's controller controls none")
    void noLandMeansNoSacrifice() {
        Permanent lavaRunner = harness.addToBattlefieldAndReturn(player1, new LavaRunner());
        harness.addToBattlefield(player2, new ProdigalPyromancer());

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, lavaRunner.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lava Runner");
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
