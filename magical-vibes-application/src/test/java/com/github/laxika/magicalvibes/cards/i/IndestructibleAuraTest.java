package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        IndestructibleAura.class,
        DurkwoodBoars.class,
        Karakas.class,
        KoboldsOfKherKeep.class,
        PsionicEntity.class
})
class IndestructibleAuraTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage dealt to the target creature this turn")
    void preventsAllDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());

        castIndestructibleAura(creature);
        dealTwoDamageTo(creature);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player1, "Kobolds of Kher Keep");
    }

    @Test
    @DisplayName("Prevents multiple damage events to the target creature this turn")
    void preventsMultipleDamageEvents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());

        castIndestructibleAura(creature);
        dealTwoDamageTo(creature);
        dealTwoDamageTo(creature);

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player1, "Kobolds of Kher Keep");
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the target creature")
    void preventsCombatDamageToTargetCreature() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBoars());
        Permanent target = addCreatureReady(player2, new DurkwoodBoars());

        castIndestructibleAura(target);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Durkwood Boars");
    }

    @Test
    @DisplayName("Prevention wears off at end of turn")
    void preventionWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());

        castIndestructibleAura(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        dealTwoDamageTo(creature);

        harness.assertInGraveyard(player1, "Kobolds of Kher Keep");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent karakas = harness.addToBattlefieldAndReturn(player1, new Karakas());
        harness.setHand(player1, List.of(new IndestructibleAura()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID karakasId = karakas.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, karakasId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castIndestructibleAura(Permanent target) {
        harness.setHand(player1, List.of(new IndestructibleAura()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void dealTwoDamageTo(Permanent target) {
        Permanent source = addCreatureReady(player1, new PsionicEntity());
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
