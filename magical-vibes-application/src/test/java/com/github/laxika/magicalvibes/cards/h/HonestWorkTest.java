package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonestWork.class, AirElemental.class, FountainOfYouth.class})
class HonestWorkTest extends BaseCardTest {

    @Test
    @DisplayName("Honest Work taps the enchanted creature and removes all its counters")
    void entersTappingCreatureAndRemovingCounters() {
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        elemental.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        elemental.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new HonestWork()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, elemental.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Honest Work turns the enchanted creature into a Humble Merchant")
    void transformsEnchantedCreature() {
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        Permanent aura = new Permanent(new HonestWork());
        aura.setAttachedTo(elemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, elemental);

        assertThat(gqs.getEffectiveName(gd, elemental)).isEqualTo("Humble Merchant");
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(bonus.subtypeOverriding()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature can use the granted colorless mana ability")
    void grantsColorlessManaAbility() {
        Permanent elemental = addCreatureReady(player2, new AirElemental());
        Permanent aura = new Permanent(new HonestWork());
        aura.setAttachedTo(elemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Honest Work can target only a creature an opponent controls")
    void rejectsIllegalTargets() {
        Permanent ownCreature = addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new HonestWork()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }
}
