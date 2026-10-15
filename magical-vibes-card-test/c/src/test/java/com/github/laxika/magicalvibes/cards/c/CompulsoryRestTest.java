package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.OraclesVault;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

@CardUsed({CompulsoryRest.class, Colossapede.class, OraclesVault.class})
class CompulsoryRestTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Compulsory Rest attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        creature.setSummoningSick(false);

        harness.setHand(player1, List.of(new CompulsoryRest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Compulsory Rest")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Colossapede()); // a legal target exists, so the card is playable
        harness.addToBattlefield(player1, new OraclesVault());
        harness.setHand(player1, List.of(new CompulsoryRest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = findPermanent(player1, "Oracle's Vault");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        creature.setSummoningSick(false);

        Permanent rest = harness.addToBattlefieldAndReturn(player2, new CompulsoryRest());
        rest.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        blocker.setSummoningSick(false);

        Permanent rest = harness.addToBattlefieldAndReturn(player1, new CompulsoryRest());
        rest.setAttachedTo(blocker.getId());

        // Player1 has an attacker (index 1, after Compulsory Rest at index 0)
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature's controller can pay {2} and sacrifice it to gain 2 life")
    void grantedAbilitySacrificesCreatureAndGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        creature.setSummoningSick(false);

        Permanent rest = harness.addToBattlefieldAndReturn(player1, new CompulsoryRest());
        rest.setAttachedTo(creature.getId());

        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Activate the granted ability on the CREATURE (index 0)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Colossapede");
    }

    @Test
    @DisplayName("Opponent gains the life and sacrifices a tapped, summoning-sick enchanted creature as a cost")
    void opponentCanActivateBeforeUntappingOrLosingSummoningSickness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        creature.setSummoningSick(true);
        creature.tap();
        Permanent rest = harness.addToBattlefieldAndReturn(player1, new CompulsoryRest());
        rest.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player2, 0, null, null);

        harness.assertNotOnBattlefield(player2, "Colossapede");
        harness.assertInGraveyard(player2, "Colossapede");
        harness.assertLife(player2, 10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Compulsory Rest");
    }

    @Test
    @DisplayName("The granted ability requires two mana before the creature is sacrificed")
    void insufficientManaDoesNotSacrificeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent rest = harness.addToBattlefieldAndReturn(player1, new CompulsoryRest());
        rest.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Colossapede");
        harness.assertOnBattlefield(player1, "Compulsory Rest");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
