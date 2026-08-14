package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnimateArtifact;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StasisCocoonTest extends BaseCardTest {

    @Test
    @DisplayName("Stasis Cocoon can enchant an artifact")
    void canEnchantArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new StasisCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof StasisCocoon
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Stasis Cocoon cannot enchant a nonartifact permanent")
    void cannotEnchantNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StasisCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Enchanted artifact cannot activate abilities")
    void enchantedArtifactCannotActivateAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        addAura(player2, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted artifact creature cannot attack")
    void enchantedArtifactCreatureCannotAttack() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        artifact.setSummoningSick(false);
        addAura(player1, artifact, new AnimateArtifact());
        addAura(player2, artifact);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted artifact creature cannot block")
    void enchantedArtifactCreatureCannotBlock() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        artifact.setSummoningSick(false);
        addAura(player2, artifact, new AnimateArtifact());
        addAura(player1, artifact);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent host) {
        return addAura(controller, host, new StasisCocoon());
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller, Permanent host, Card auraCard) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, auraCard);
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
