package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.u.UlamogsCrusher;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RapaciousOne.class, UlamogsCrusher.class, ReinforcedBulwark.class})
class RapaciousOneTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player creates that many Eldrazi Spawn tokens")
    void createsTokensEqualToCombatDamage() {
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(5);
    }

    @Test
    @DisplayName("An Eldrazi Spawn created by Rapacious One can be sacrificed for colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("No Eldrazi Spawn tokens are created without combat damage to a player")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UlamogsCrusher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Created Spawn are untapped colorless 0/1 Eldrazi Spawn creatures")
    void createsCorrectSpawnTokens() {
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(5).allSatisfy(spawn -> {
            assertThat(spawn.getCard().isToken()).isTrue();
            assertThat(spawn.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spawn.getCard().getColors()).isEmpty();
            assertThat(spawn.getCard().getPower()).isZero();
            assertThat(spawn.getCard().getToughness()).isEqualTo(1);
            assertThat(spawn.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(spawn.isTapped()).isFalse();
            assertThat(spawn.isAttacking()).isFalse();
        });
    }

    @Test
    @DisplayName("The combat damage trigger creates Spawn even after Rapacious One leaves")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(rapaciousOne);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(5);
    }

    @Test
    @DisplayName("Only trample damage dealt to the player creates Spawn")
    void createsTokensForTrampleDamageOnly() {
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ReinforcedBulwark());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Prevented combat damage does not contribute to the Spawn count")
    void createsTokensForUnpreventedDamageOnly() {
        addCreatureReady(player2, new ReinforcedBulwark());
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(4);
    }

    @Test
    @DisplayName("Fully preventing the trample damage creates no Spawn")
    void fullyPreventedTrampleDamageDoesNotTrigger() {
        Permanent blocker = addCreatureReady(player2, new ReinforcedBulwark());
        addCreatureReady(player2, new ReinforcedBulwark());
        harness.activateAbility(player2, 1, null, null);
        resolveAllTriggers();
        Permanent rapaciousOne = addCreatureReady(player1, new RapaciousOne());
        rapaciousOne.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }
}
