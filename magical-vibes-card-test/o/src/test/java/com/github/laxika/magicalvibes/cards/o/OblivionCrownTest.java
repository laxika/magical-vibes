package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OblivionCrown.class, BlindPhantasm.class, HorizonCanopy.class})
class OblivionCrownTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card activates the enchanted creature's +1/+1 ability")
    void discardingCardBoostsEnchantedCreature() {
        Permanent creature = addEnchantedCreature();
        BlindPhantasm discarded = new BlindPhantasm();
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The granted boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addEnchantedCreature();
        harness.setHand(player1, List.of(new BlindPhantasm()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The granted ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addEnchantedCreature();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enchanted creature's controller can activate the granted ability")
    void enchantedCreatureControllerCanActivateGrantedAbility() {
        Permanent creature = addCreatureReady(player2, new BlindPhantasm());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OblivionCrown());
        aura.setAttachedTo(creature.getId());
        BlindPhantasm discarded = new BlindPhantasm();
        harness.setHand(player2, List.of(discarded));

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Oblivion Crown can target only a creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HorizonCanopy());
        harness.setHand(player1, List.of(new OblivionCrown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Flash allows Oblivion Crown to be cast during upkeep")
    void canCastDuringUpkeep() {
        Permanent creature = addCreatureReady(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new OblivionCrown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Oblivion Crown");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        harness.setHand(player1, List.of(new HorizonCanopy()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped summoning-sick creature can repeatedly discard any card for cumulative boosts")
    void repeatedActivationsDoNotRequireTapOrMana() {
        Permanent creature = addEnchantedCreature();
        creature.tap();
        creature.setSummoningSick(true);
        BlindPhantasm discardedCreature = new BlindPhantasm();
        HorizonCanopy discardedLand = new HorizonCanopy();
        harness.setHand(player1, List.of(discardedCreature, discardedLand));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCreature);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCreature, discardedLand);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new BlindPhantasm());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OblivionCrown());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
