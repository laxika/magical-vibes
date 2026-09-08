package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraAviary.class, WillowFaerie.class, BeastWalkers.class})
class SerraAviaryTest extends BaseCardTest {

    @Test
    @DisplayName("Own creatures with flying get +1/+1")
    void buffsOwnFliers() {
        harness.addToBattlefield(player1, new SerraAviary());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WillowFaerie());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent creatures with flying also get +1/+1")
    void buffsOpponentFliers() {
        harness.addToBattlefield(player1, new SerraAviary());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new WillowFaerie());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures without flying are unaffected")
    void doesNotBuffGroundCreatures() {
        harness.addToBattlefield(player1, new SerraAviary());
        Permanent walkers = harness.addToBattlefieldAndReturn(player1, new BeastWalkers());

        assertThat(gqs.getEffectivePower(gd, walkers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walkers)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Serra Aviary leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent aviary = harness.addToBattlefieldAndReturn(player1, new SerraAviary());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WillowFaerie());
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(aviary);

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("A flying creature version of Serra Aviary buffs itself")
    void buffsItselfWhenItBecomesAFlyingCreature() {
        SerraAviary card = new SerraAviary();
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        card.setPower(4);
        card.setToughness(4);
        card.setKeywords(Set.of(Keyword.FLYING));
        Permanent aviary = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.hasKeyword(gd, aviary, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, aviary)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aviary)).isEqualTo(5);
    }
}
