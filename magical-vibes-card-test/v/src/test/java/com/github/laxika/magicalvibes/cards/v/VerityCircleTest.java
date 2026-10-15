package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerityCircle.class, GrizzlyBears.class, Island.class, SerraAngel.class})
class VerityCircleTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an opponent's creature lets the controller draw")
    void opponentCreatureTapDraws() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        tap(creature);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Tapping an opponent's creature while declaring it as an attacker does not trigger")
    void attackerDeclarationDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerityCircle());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping an attacking vigilance creature does trigger")
    void tappingAttackingVigilanceCreatureTriggers() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent attacker = addCreatureReady(player2, new SerraAngel());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        tap(attacker);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Tapping your own creature does not trigger")
    void ownCreatureTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(creature);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability taps a target creature without flying")
    void abilityTapsNonflyingCreature() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a creature with flying")
    void abilityCannotTargetFlyingCreature() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = addCreatureReady(player2, new SerraAngel());
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature")
    void abilityCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    @DisplayName("Drawing after tapping an opponent's creature is optional")
    void mayDeclineDrawFromActivatedTap() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability triggers a draw when it taps an opponent's creature")
    void activatedTapDraws() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping an already tapped creature does not trigger a draw")
    void alreadyTappedCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        harness.addMana(player1, ManaColor.BLUE, 5);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Tapping an opponent's noncreature does not trigger")
    void opponentNoncreatureTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new VerityCircle());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        tap(island);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
