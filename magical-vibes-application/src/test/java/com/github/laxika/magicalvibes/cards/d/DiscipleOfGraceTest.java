package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogRaiders;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscipleOfGrace.class, Expunge.class, GorillaWarrior.class, BogRaiders.class})
class DiscipleOfGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from black prevents black spells from targeting Disciple of Grace")
    void protectionFromBlackPreventsBlackSpellTargeting() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new DiscipleOfGrace());

        harness.setHand(player1, List.of(new Expunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking")
    void protectionFromBlackPreventsBlocking() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfGrace());
        disciple.setAttacking(true);
        addCreatureReady(player2, new BogRaiders());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new BogRaiders());
        attacker.setAttacking(true);
        Permanent disciple = addCreatureReady(player2, new DiscipleOfGrace());
        disciple.setBlocking(true);
        disciple.addBlockingTarget(0);

        resolveCombat();

        assertThat(disciple.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards Disciple of Grace and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DiscipleOfGrace()));
        harness.setLibrary(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Grace");
        harness.assertInHand(player1, "Gorilla Warrior");
    }
}
