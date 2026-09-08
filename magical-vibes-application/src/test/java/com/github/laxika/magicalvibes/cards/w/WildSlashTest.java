package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WildSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage that can be prevented without ferocious")
    void damageCanBePreventedWithoutFerocious() {
        addCreature(player1, 3, 3);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        castWildSlash();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(8);
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    @Test
    @DisplayName("With ferocious, damage cannot be prevented this turn")
    void damageCannotBePreventedWithFerocious() {
        addCreature(player1, 4, 4);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        castWildSlash();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
    }

    @Test
    @DisplayName("Checks ferocious as Wild Slash resolves")
    void checksFerociousAtResolution() {
        addCreature(player1, 4, 4);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(8);
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    private void castWildSlash() {
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void addCreature(com.github.laxika.magicalvibes.model.Player player, int power, int toughness) {
        Card creature = new Card();
        creature.setName("Creature");
        creature.setType(CardType.CREATURE);
        creature.setPower(power);
        creature.setToughness(toughness);
        harness.addToBattlefield(player, creature);
    }
}
