package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DepartedDeckhand.class, GreenwoodSentinel.class, Shock.class, Disperse.class})
class DepartedDeckhandTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent deckhand = addReadyDeckhand(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, deckhand.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(deckhand.getId()));
        harness.assertInGraveyard(player1, "Departed Deckhand");
    }

    @Test
    @DisplayName("Cannot be blocked by a non-Spirit creature")
    void cannotBeBlockedByNonSpirit() {
        Permanent deckhand = addReadyDeckhand(player1);
        deckhand.setAttacking(true);
        addReadySentinel(player2);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirits");
    }

    @Test
    @DisplayName("Can be blocked by a Spirit")
    void canBeBlockedBySpirit() {
        Permanent deckhand = addReadyDeckhand(player1);
        deckhand.setAttacking(true);
        Permanent spirit = addReadyDeckhand(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spirit.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability makes another creature you control blockable only by Spirits")
    void abilityRestrictsBlockersOnTargetCreature() {
        addReadyDeckhand(player1);
        Permanent bear = addReadySentinel(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        bear.setAttacking(true);
        addReadySentinel(player2);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirits");
    }

    @Test
    @DisplayName("The ability cannot target the Deckhand itself")
    void abilityCannotTargetItself() {
        Permanent deckhand = addReadyDeckhand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, deckhand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("other than this creature");
    }

    @Test
    @DisplayName("The ability cannot target a creature an opponent controls")
    void abilityCannotTargetOpponentCreature() {
        addReadyDeckhand(player1);
        Permanent enemyBear = addReadySentinel(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificesBeforeOpponentsBounceSpellResolves() {
        Permanent deckhand = addReadyDeckhand(player1);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, deckhand.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Departed Deckhand");
        harness.assertNotInHand(player1, "Departed Deckhand");
        harness.assertNotOnBattlefield(player1, "Departed Deckhand");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Disperse");
        harness.assertNotInHand(player1, "Departed Deckhand");
    }

    @Test
    void sacrificesWhenTargetedByControllersSpell() {
        Permanent deckhand = addReadyDeckhand(player1);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, deckhand.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Departed Deckhand");
        harness.assertNotInHand(player1, "Departed Deckhand");
    }

    @Test
    void targetingAnotherDeckhandWithAbilityDoesNotSacrificeIt() {
        addReadyDeckhand(player1);
        Permanent target = addReadyDeckhand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(target.getId());
        harness.assertNotInGraveyard(player1, "Departed Deckhand");
    }

    @Test
    void abilityAllowsSpiritToBlockTarget() {
        addReadyDeckhand(player1);
        Permanent target = addReadySentinel(player1);
        Permanent spirit = addReadyDeckhand(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(spirit.isBlocking()).isTrue();
    }

    @Test
    void abilityRestrictionExpiresAtEndOfTurn() {
        addReadyDeckhand(player1);
        Permanent target = addReadySentinel(player1);
        Permanent blocker = addReadySentinel(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyDeckhand(Player player) {
        return addCreatureReady(player, new DepartedDeckhand());
    }

    private Permanent addReadySentinel(Player player) {
        return addCreatureReady(player, new GreenwoodSentinel());
    }
}
