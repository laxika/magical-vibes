package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({DiscipleOfMalice.class, Pacifism.class, GlorySeeker.class})
class DiscipleOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from white prevents white spells from targeting Disciple of Malice")
    void protectionFromWhitePreventsWhiteSpellTargeting() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new DiscipleOfMalice());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from white prevents a white creature from blocking Disciple of Malice")
    void protectionFromWhitePreventsBlocking() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfMalice());
        disciple.setAttacking(true);
        addCreatureReady(player2, new GlorySeeker());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from white prevents combat damage from a white creature")
    void protectionFromWhitePreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        attacker.setAttacking(true);
        Permanent disciple = addCreatureReady(player2, new DiscipleOfMalice());
        disciple.setBlocking(true);
        disciple.addBlockingTarget(0);

        resolveCombat();

        assertThat(disciple.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards Disciple of Malice and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DiscipleOfMalice()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Malice");
        harness.assertInHand(player1, "Glory Seeker");
    }
}
