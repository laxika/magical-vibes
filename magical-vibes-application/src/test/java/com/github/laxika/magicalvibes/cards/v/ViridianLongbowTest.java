package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianLongbow.class, AlphaMyr.class})
class ViridianLongbowTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Viridian Longbow to a creature")
    void equipAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(longbow.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void equipCannotTargetNoncreaturePermanent() {
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, longbow.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 1 damage to a player")
    void equippedCreatureDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("damage from Alpha Myr"));
    }

    @Test
    @DisplayName("Equipped creature can deal 1 damage to a creature")
    void equippedCreatureDealsDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(creature.getId());
        addCreatureReady(player2, new AlphaMyr());

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Alpha Myr"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("Detaching Viridian Longbow removes the granted ability")
    void detachingRemovesGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(creature.getId());

        longbow.setAttachedTo(null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
