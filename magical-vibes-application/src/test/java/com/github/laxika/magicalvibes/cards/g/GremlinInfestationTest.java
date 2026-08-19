package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GremlinInfestationTest extends BaseCardTest {

    @Test
    void dealsDamageToEnchantedArtifactControllerOnAurasControllerEndStep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        attachAura(player1, artifact);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        runEndStep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 2);
    }

    @Test
    void doesNotDealDamageOnEnchantedArtifactsControllerEndStep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        attachAura(player1, artifact);
        int player2Life = gd.getLife(player2.getId());

        runEndStep(player2);

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life);
    }

    @Test
    void createsGremlinWhenEnchantedArtifactIsPutIntoGraveyard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        attachAura(player1, artifact);
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gremlin")).hasSize(1);
    }

    @Test
    void cannotEnchantNonArtifactPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GremlinInfestation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void runEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent attachAura(Player controller, Permanent artifact) {
        Permanent aura = new Permanent(new GremlinInfestation());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
