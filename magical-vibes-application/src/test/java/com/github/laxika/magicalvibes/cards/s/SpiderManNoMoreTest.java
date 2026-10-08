package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderManNoMore.class, AirElemental.class, FountainOfYouth.class, ProdigalPyromancer.class})
class SpiderManNoMoreTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiderManNoMore());
        aura.setAttachedTo(elemental.getId());

        assertThat(gqs.effectiveCreatureSubtypes(gd, elemental))
                .containsExactly(CardSubtype.CITIZEN);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void removingAuraRestoresCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiderManNoMore());
        aura.setAttachedTo(elemental.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.effectiveCreatureSubtypes(gd, elemental))
                .containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.DEFENDER)).isFalse();
    }

    @Test
    void resolvesOnOwnCreatureAndRetainsDefender() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new SpiderManNoMore()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, elemental.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider-Man No More");
        assertThat(gqs.effectiveCreatureSubtypes(gd, elemental)).containsExactly(CardSubtype.CITIZEN);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();
    }

    @Test
    void countersStillModifyTheNewBasePowerAndToughness() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        elemental.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiderManNoMore());
        aura.setAttachedTo(elemental.getId());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void removesActivatedAbilityAndRestoresItWhenAuraLeaves() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiderManNoMore());
        aura.setAttachedTo(pyromancer.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pyromancer.isTapped()).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void rejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SpiderManNoMore()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
