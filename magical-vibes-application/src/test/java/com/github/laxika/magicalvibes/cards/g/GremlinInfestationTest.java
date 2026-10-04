package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.Memnarch;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GremlinInfestation.class, FountainOfYouth.class, Shatter.class, GrizzlyBears.class,
        Boomerang.class, Disenchant.class, Memnarch.class})
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

    @Test
    void canCastAndAttachToArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GremlinInfestation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Gremlin Infestation").getAttachedTo()).isEqualTo(artifact.getId());
        runEndStep(player1);
        harness.assertLife(player2, 18);
    }

    @Test
    void returningArtifactToHandDoesNotCreateGremlin() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        attachAura(player1, artifact);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Gremlin Infestation");
        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
        assertThat(findPermanents(player2, "Gremlin")).isEmpty();
    }

    @Test
    void destroyingAuraDoesNotCreateGremlin() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent aura = attachAura(player1, artifact);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Gremlin Infestation");
        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
        runEndStep(player1);
        harness.assertLife(player2, 20);
    }

    @Test
    void pendingDamageStillResolvesAfterArtifactIsDestroyed() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        attachAura(player1, artifact);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(findPermanents(player1, "Gremlin")).hasSize(1);
        assertThat(findPermanents(player2, "Gremlin")).isEmpty();
        harness.assertInGraveyard(player1, "Gremlin Infestation");
    }

    @Test
    void pendingDamageStillResolvesAfterAuraIsDestroyed() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent aura = attachAura(player1, artifact);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
    }

    @Test
    void pendingDamageUsesArtifactsNewController() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new Memnarch());
        attachAura(player1, artifact);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void pendingDamageUsesLastControllerWhenArtifactChangesControlThenDies() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new Memnarch());
        attachAura(player1, artifact);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, artifact.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(findPermanents(player1, "Gremlin")).hasSize(1);
    }

    private void runEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private Permanent attachAura(Player controller, Permanent artifact) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new GremlinInfestation());
        aura.setAttachedTo(artifact.getId());
        return aura;
    }
}
