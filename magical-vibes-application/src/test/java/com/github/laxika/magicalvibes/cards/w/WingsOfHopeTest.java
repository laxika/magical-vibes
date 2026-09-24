package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingsOfHope.class, BenalishLancer.class, CoastalTower.class})
class WingsOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Wings of Hope attaches to the target creature")
    void attachesToTargetCreature() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());

        harness.setHand(player1, List.of(new WingsOfHope()));
        addMana();

        harness.castEnchantment(player1, 0, lancer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wings of Hope")
                        && p.isAttached()
                        && lancer.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Wings of Hope can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent opponentLancer = harness.addToBattlefieldAndReturn(player2, new BenalishLancer());

        harness.setHand(player1, List.of(new WingsOfHope()));
        addMana();

        harness.castEnchantment(player1, 0, opponentLancer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wings of Hope")
                        && opponentLancer.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, opponentLancer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentLancer)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, opponentLancer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+3 and flying")
    void boostsAndGrantsFlying() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingsOfHope());
        aura.setAttachedTo(lancer.getId());

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Wings of Hope does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingsOfHope());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wings of Hope stops affecting the creature when removed")
    void effectsStopWhenRemoved() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WingsOfHope());
        aura.setAttachedTo(lancer.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, lancer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lancer)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wings of Hope fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent lancer = harness.addToBattlefieldAndReturn(player1, new BenalishLancer());

        harness.setHand(player1, List.of(new WingsOfHope()));
        addMana();

        harness.castEnchantment(player1, 0, lancer.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lancer);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wings of Hope");
        harness.assertNotOnBattlefield(player1, "Wings of Hope");
    }

    @Test
    @DisplayName("Wings of Hope cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CoastalTower());
        harness.setHand(player1, List.of(new WingsOfHope()));
        addMana();

        Permanent nonCreature = findPermanent(player1, "Coastal Tower");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
