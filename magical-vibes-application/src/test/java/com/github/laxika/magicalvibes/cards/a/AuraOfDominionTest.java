package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraOfDominion.class, GrizzlyBears.class})
class AuraOfDominionTest extends BaseCardTest {

    @Test
    @DisplayName("Ability untaps the enchanted creature and taps the creature paid as a cost")
    void untapsEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchanted.tap();
        Permanent tapFodder = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIdx, null, null);
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(tapFodder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature itself can be tapped to pay the cost")
    void enchantedCreatureCanPayItsOwnCost() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIdx, null, null);
        harness.passBothPriorities();

        // Tapped as a cost, then untapped on resolution.
        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature to tap")
    void cannotActivateWithoutUntappedCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchanted.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures an opponent controls cannot pay the cost")
    void opponentCreaturesCannotPayCost() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchanted.tap();
        addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, auraIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses the last known enchanted creature if the Aura leaves before resolution")
    void usesLastKnownEnchantedCreatureWhenAuraLeavesBeforeResolution() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchanted.tap();
        Permanent tapFodder = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AuraOfDominion());
        aura.setAttachedTo(enchanted.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        int auraIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aura);

        harness.activateAbility(player1, auraIdx, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(tapFodder.isTapped()).isTrue();
    }
}
