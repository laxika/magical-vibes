package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({WhispersteelDagger.class, GrizzlyBears.class})
class WhispersteelDaggerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new WhispersteelDagger());
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches the Dagger to a creature you control")
    void equipAttachesToCreature() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new WhispersteelDagger());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int daggerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dagger);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, daggerIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Combat damage grants one later creature cast from the damaged player's graveyard")
    void combatDamageGrantsOneNormalCostAnyColorCreatureCast() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature));

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new WhispersteelDagger());
        dagger.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(firstCreature.getId()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, secondCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from graveyard");
    }

    @Test
    @DisplayName("Combat damage does not grant access to the controller's graveyard")
    void combatDamageDoesNotGrantOwnGraveyardAccess() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new WhispersteelDagger());
        dagger.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cast from graveyard");
    }
}
