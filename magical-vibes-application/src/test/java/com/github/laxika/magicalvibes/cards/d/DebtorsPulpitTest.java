package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DebtorsPulpit.class, Forest.class, GrizzlyBears.class})
class DebtorsPulpitTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land can tap to tap target creature")
    void enchantedLandTapsTargetCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DebtorsPulpit());
        aura.setAttachedTo(forest.getId());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability can target a creature controlled by either player")
    void grantedAbilityCanTargetOwnCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DebtorsPulpit());
        aura.setAttachedTo(forest.getId());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability cannot target a land")
    void grantedAbilityCannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DebtorsPulpit());
        aura.setAttachedTo(forest.getId());
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherForest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isFalse();
        assertThat(otherForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting the Aura on a land grants the ability after resolution")
    void castingAuraGrantsAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DebtorsPulpit()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Debtor's Pulpit").getAttachedTo()).isEqualTo(forest.getId());
        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(forest.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchanted land grants its ability to that opponent")
    void opponentControlsAbilityGrantedToTheirLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DebtorsPulpit()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Debtor's Pulpit").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Aura cannot be cast targeting a nonland creature")
    void auraCannotEnchantNonlandCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DebtorsPulpit()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An already tapped enchanted land cannot pay the granted ability's cost")
    void tappedLandCannotActivateGrantedAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DebtorsPulpit());
        aura.setAttachedTo(forest.getId());
        forest.tap();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted ability can target an already tapped creature")
    void alreadyTappedCreatureIsLegalTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DebtorsPulpit());
        aura.setAttachedTo(forest.getId());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
