package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
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

@CardUsed({PaladinsShield.class, DireWolfProwler.class})
class PaladinsShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Paladin's Shield to be cast during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new PaladinsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Entering Paladin's Shield attaches it and grants +0/+2")
    void enteringAttachesAndBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.setHand(player1, List.of(new PaladinsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        Permanent shield = findPermanent(player1, "Paladin's Shield");
        assertThat(shield.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip moves Paladin's Shield to another creature")
    void equipMovesShieldToAnotherCreature() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        Permanent shield = harness.addToBattlefieldAndReturn(player1, new PaladinsShield());
        shield.setAttachedTo(firstBear.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, null, secondBear.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(secondBear.getId());
        assertThat(gqs.getEffectiveToughness(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondBear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Paladin's Shield cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new DireWolfProwler());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        harness.setHand(player1, List.of(new PaladinsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The Shield can enter without a creature to attach to")
    void entersWithoutLegalTargets() {
        harness.addToBattlefield(player2, new DireWolfProwler());
        harness.setHand(player1, List.of(new PaladinsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Paladin's Shield").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Losing the attach trigger's target leaves the Shield on the battlefield")
    void losingTriggerTargetLeavesShieldUnattached() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.setHand(player1, List.of(new PaladinsShield()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Paladin's Shield").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new PaladinsShield());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Flash does not allow equip during combat")
    void equipCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new PaladinsShield());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
