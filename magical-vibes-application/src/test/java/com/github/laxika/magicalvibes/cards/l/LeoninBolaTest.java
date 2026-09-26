package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninBola.class, AuriokGlaivemaster.class, DarksteelCitadel.class})
class LeoninBolaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature can unattach Leonin Bola to tap a target creature")
    void unattachAndTapTargetCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new AuriokGlaivemaster());
        Permanent bola = harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        bola.setAttachedTo(equippedCreature.getId());
        Permanent target = addCreatureReady(player2, new AuriokGlaivemaster());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(equippedCreature.isTapped()).isTrue();
        assertThat(bola.getAttachedTo()).isNull();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Leonin Bola cannot target a land")
    void cannotTargetLand() {
        Permanent equippedCreature = addCreatureReady(player1, new AuriokGlaivemaster());
        Permanent bola = harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        bola.setAttachedTo(equippedCreature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Equip ability attaches Leonin Bola to a creature you control for {1}")
    void equipAttachesToCreatureYouControl() {
        Permanent bola = harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        Permanent creature = addCreatureReady(player1, new AuriokGlaivemaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bola.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Leonin Bola equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        Permanent opponentCreature = addCreatureReady(player2, new AuriokGlaivemaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Leonin Bola equip is sorcery speed")
    void equipIsSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        Permanent creature = addCreatureReady(player1, new AuriokGlaivemaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("An unattached Leonin Bola grants no tap ability")
    void unattachedBolaGrantsNoAbility() {
        addCreatureReady(player1, new AuriokGlaivemaster());
        Permanent bola = harness.addToBattlefieldAndReturn(player1, new LeoninBola());
        Permanent target = addCreatureReady(player2, new AuriokGlaivemaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bola.getAttachedTo()).isNull();
    }
}
