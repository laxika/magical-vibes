package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DetentionVortex.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, CampusGuide.class, LetterOfAcceptance.class})
class DetentionVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, creature);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate abilities")
    void enchantedPermanentCannotActivateAbilities() {
        addCreatureReady(player2, new LlanowarElves());
        attachAura(player1, findPermanent(player2, "Llanowar Elves"));

        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
    }

    @Test
    @DisplayName("Only an opponent may activate the sorcery-speed destruction ability")
    void onlyOpponentMayDestroyDuringSorcerySpeed() {
        attachAura(player1, addCreatureReady(player1, new CampusGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only your opponents may activate");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("An opponent can pay three mana to destroy Detention Vortex")
    void opponentCanDestroyAura() {
        attachAura(player1, addCreatureReady(player1, new CampusGuide()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareMainPhase(player2);

        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Detention Vortex");
        harness.assertInGraveyard(player1, "Detention Vortex");
    }

    @Test
    @DisplayName("Detention Vortex cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DetentionVortex()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void canEnchantNoncreatureArtifactAndLockBothAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new DetentionVortex()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase(player1);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Detention Vortex").getAttachedTo()).isEqualTo(artifact.getId());
        prepareMainPhase(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
        harness.assertOnBattlefield(player2, "Letter of Acceptance");
    }

    @Test
    void opponentCannotDestroyAuraDuringAnotherPlayersMainPhase() {
        attachAura(player1, addCreatureReady(player1, new CampusGuide()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Detention Vortex");
    }

    @Test
    void opponentCannotActivateDestructionWithNonemptyStack() {
        Permanent creature = addCreatureReady(player1, new CampusGuide());
        attachAura(player1, creature);
        harness.setHand(player2, List.of(new DetentionVortex()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        prepareMainPhase(player2);
        harness.castEnchantment(player2, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void destructionRemovesAbilityLockAndRequiresFullPayment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LetterOfAcceptance());
        attachAura(player1, artifact);
        prepareMainPhase(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertOnBattlefield(player1, "Detention Vortex");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 1, null, null);
        harness.assertOnBattlefield(player1, "Detention Vortex");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Detention Vortex");

        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Letter of Acceptance");
    }

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller,
                                  Permanent enchanted) {
        Permanent aura = addAura(controller);
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    private Permanent addAura(com.github.laxika.magicalvibes.model.Player controller) {
        return harness.addToBattlefieldAndReturn(controller, new DetentionVortex());
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
