package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GleamOfAuthority.class, com.github.laxika.magicalvibes.cards.g.GrizzlyBears.class,
        com.github.laxika.magicalvibes.cards.f.FountainOfYouth.class, HillGiant.class})
class GleamOfAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each counter on other creatures and vigilance")
    void boostsForCountersOnOtherCreatures() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        enchantedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent firstOther = addCreatureReady(player1, new GrizzlyBears());
        firstOther.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent secondOther = addCreatureReady(player1, new GrizzlyBears());
        secondOther.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        addAura(enchantedCreature);

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, firstOther)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, firstOther, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can tap to bolster the least-tough creature")
    void enchantedCreatureCanBolster() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new HillGiant());
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAura(enchantedCreature);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gleam of Authority cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        Permanent artifact = findPermanent(player1, "Fountain of Youth");
        harness.setHand(player1, List.of(new GleamOfAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GleamOfAuthority());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
