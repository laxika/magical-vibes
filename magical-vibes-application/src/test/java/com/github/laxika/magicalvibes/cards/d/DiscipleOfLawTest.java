package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Acridian;
import com.github.laxika.magicalvibes.cards.f.FieryMantle;
import com.github.laxika.magicalvibes.cards.f.FireAnts;
import com.github.laxika.magicalvibes.cards.s.ShowerOfSparks;
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

@CardUsed({DiscipleOfLaw.class, Acridian.class, FireAnts.class, ShowerOfSparks.class, FieryMantle.class})
class DiscipleOfLawTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from red prevents red spells from targeting Disciple of Law")
    void protectionFromRedPreventsRedSpellTargeting() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new DiscipleOfLaw());

        harness.setHand(player1, List.of(new ShowerOfSparks()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(disciple.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from red prevents red creatures from blocking Disciple of Law")
    void protectionFromRedPreventsBlocking() {
        Permanent disciple = addCreatureReady(player1, new DiscipleOfLaw());
        disciple.setAttacking(true);
        addCreatureReady(player2, new FireAnts());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from red creatures")
    void protectionFromRedPreventsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new FireAnts());
        attacker.setAttacking(true);
        Permanent disciple = addCreatureReady(player2, new DiscipleOfLaw());
        disciple.setBlocking(true);
        disciple.addBlockingTarget(0);

        resolveCombat();

        assertThat(disciple.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from red prevents damage from red sources")
    void protectionFromRedPreventsDamage() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player2, new DiscipleOfLaw());
        addCreatureReady(player1, new FireAnts());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(disciple.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cycling discards Disciple of Law and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DiscipleOfLaw()));
        harness.setLibrary(player1, List.of(new Acridian()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Disciple of Law");
        harness.assertInHand(player1, "Acridian");
    }

    @Test
    @DisplayName("Protection from red prevents even its controller's red Aura from targeting Disciple of Law")
    void protectionFromRedPreventsOwnAuraTargeting() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfLaw());
        harness.setHand(player1, List.of(new FieryMantle()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, disciple.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cycling discards as an activation cost and draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new DiscipleOfLaw()));
        harness.setLibrary(player1, List.of(new Acridian()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Disciple of Law");
        harness.assertNotInHand(player1, "Disciple of Law");
        harness.assertNotInHand(player1, "Acridian");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Acridian");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling requires two mana and does not discard when the cost cannot be paid")
    void cyclingCannotBeActivatedWithOnlyOneMana() {
        harness.setHand(player1, List.of(new DiscipleOfLaw()));
        harness.setLibrary(player1, List.of(new Acridian()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Disciple of Law");
        harness.assertNotInGraveyard(player1, "Disciple of Law");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
