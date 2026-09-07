package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GratuitousViolence.class, ProdigalSorcerer.class, Shock.class})
class GratuitousViolenceTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles noncombat damage dealt by a creature you control")
    void doublesNoncombatDamageDealtByControlledCreature() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage from a noncreature source")
    void doesNotDoubleDamageFromNoncreatureSource() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage dealt by an opponent's creature")
    void doesNotDoubleDamageDealtByOpponentsCreature() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }
}
