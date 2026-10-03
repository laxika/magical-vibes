package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlchemistsVial.class, TimberpackWolf.class})
class AlchemistsVialTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void entersDrawsCard() {
        harness.setHand(player1, List.of(new AlchemistsVial()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        // Hand: -1 for the cast artifact, +1 for the drawn card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Activating the ability sacrifices the Vial and locks the target out of attacking and blocking")
    void abilityLocksTargetOutOfCombat() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent bears = addCreatureReady(player2, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Alchemist's Vial");
        harness.assertInGraveyard(player1, "Alchemist's Vial");

        Permanent locked = findPermanent(player2, "Timberpack Wolf");
        assertThat(locked.isCantAttackThisTurn()).isTrue();
        assertThat(locked.isCantBlockThisTurn()).isTrue();
        assertThat(harness.getAttackLegalityService().canAttack(gd, locked, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Other creatures are unaffected")
    void otherCreaturesUnaffected() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent target = addCreatureReady(player2, new TimberpackWolf());
        addCreatureReady(player2, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent other = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> !p.getId().equals(target.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(other.isCantAttackThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid before the restriction resolves")
    void sacrificeIsAnActivationCost() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent target = addCreatureReady(player2, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Alchemist's Vial");
        harness.assertInGraveyard(player1, "Alchemist's Vial");
        assertThat(target.isCantAttackThisTurn()).isFalse();
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(als.canAttack(gd, target, player2.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("A tapped Vial cannot pay the activation cost")
    void tappedVialCannotActivate() {
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AlchemistsVial());
        vial.tap();
        Permanent target = addCreatureReady(player2, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Alchemist's Vial");
        harness.assertNotInGraveyard(player1, "Alchemist's Vial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activation requires one mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent target = addCreatureReady(player2, new TimberpackWolf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        harness.assertOnBattlefield(player1, "Alchemist's Vial");
        harness.assertNotInGraveyard(player1, "Alchemist's Vial");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can target a creature controlled by the Vial's controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent target = addCreatureReady(player1, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, target, player1.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();
    }

    @Test
    @DisplayName("Both restrictions expire when the turn ends")
    void restrictionsExpireAtEndOfTurn() {
        harness.addToBattlefield(player1, new AlchemistsVial());
        Permanent target = addCreatureReady(player2, new TimberpackWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(bls.canBlock(gd, target)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantAttackThisTurn()).isFalse();
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(als.canAttack(gd, target, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }
}
