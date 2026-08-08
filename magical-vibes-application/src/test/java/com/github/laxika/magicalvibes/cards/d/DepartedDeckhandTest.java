package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrchardSpirit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        addReadyBear(player2);

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
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new OrchardSpirit());
        spirit.setSummoningSick(false);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spirit.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability makes another creature you control blockable only by Spirits")
    void abilityRestrictsBlockersOnTargetCreature() {
        addReadyDeckhand(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        bear.setAttacking(true);
        addReadyBear(player2);

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
        Permanent enemyBear = addReadyBear(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDeckhand(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DepartedDeckhand());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyBear(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
    }
}
