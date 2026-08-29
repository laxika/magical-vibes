package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent blocker = addCreatureReady(player2, creature("Great Wall", 0, 6));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    private static Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
