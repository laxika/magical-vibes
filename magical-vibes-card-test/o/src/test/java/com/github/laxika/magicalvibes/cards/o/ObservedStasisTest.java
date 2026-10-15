package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IncubationDruid;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
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

@CardUsed({ObservedStasis.class, IncubationDruid.class, SwordsToPlowshares.class})
class ObservedStasisTest extends BaseCardTest {

    @Test
    @DisplayName("Observed Stasis removes the enchanted attacker and draws for tapped creatures its controller controls")
    void entersRemovesAttackerAndDrawsForTappedCreatures() {
        Permanent attacker = addCreatureReady(player2, new IncubationDruid());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        attacker.tap();
        Permanent otherTappedCreature = addCreatureReady(player2, new IncubationDruid());
        otherTappedCreature.tap();
        harness.setLibrary(player1, List.of(new IncubationDruid(), new IncubationDruid()));

        harness.setHand(player1, List.of(new ObservedStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Observed Stasis prevents the enchanted creature from attacking, blocking, or activating abilities")
    void locksEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new IncubationDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ObservedStasis());
        aura.setAttachedTo(enchanted.getId());
        harness.addMana(player2, ManaColor.GREEN, 5);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Permanent has no activated ability");

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player1, new IncubationDruid());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Observed Stasis can target only an opponent's creature")
    void targetsOnlyOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new IncubationDruid());
        harness.setHand(player1, List.of(new ObservedStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Observed Stasis draws zero when only its controller has tapped creatures")
    void ignoresOwnTappedCreaturesAndUntappedOpposingCreatures() {
        Permanent enchanted = addCreatureReady(player2, new IncubationDruid());
        addCreatureReady(player2, new IncubationDruid());
        addCreatureReady(player1, new IncubationDruid()).tap();
        harness.setLibrary(player1, List.of(new IncubationDruid()));
        harness.setHand(player1, List.of(new ObservedStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Observed Stasis counts tapped creatures when its enter trigger resolves")
    void countsAtResolution() {
        Permanent enchanted = addCreatureReady(player2, new IncubationDruid());
        Permanent other = addCreatureReady(player2, new IncubationDruid());
        harness.setLibrary(player1, List.of(new IncubationDruid(), new IncubationDruid()));
        harness.setHand(player1, List.of(new ObservedStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        enchanted.tap();
        other.tap();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Observed Stasis still draws after the enchanted creature is exiled in response")
    void drawsUsingDepartedCreaturesLastController() {
        Permanent enchanted = addCreatureReady(player2, new IncubationDruid());
        enchanted.tap();
        addCreatureReady(player2, new IncubationDruid()).tap();
        harness.setLibrary(player1, List.of(new IncubationDruid()));
        harness.setHand(player1, List.of(new ObservedStasis(), new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, enchanted.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Observed Stasis");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Observed Stasis can be cast during combat and removes a blocker without unblocking its attacker")
    void flashRemovesBlockerButAttackerRemainsBlocked() {
        Permanent attacker = addCreatureReady(player1, new IncubationDruid());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new IncubationDruid());
        blocker.setBlocking(true);
        blocker.getBlockingTargets().add(0);
        blocker.getBlockingTargetIds().add(attacker.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ObservedStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getBlockingTargets()).isEmpty();
        assertThat(blocker.getBlockingTargetIds()).isEmpty();
        assertThat(blocker.isTapped()).isFalse();
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
