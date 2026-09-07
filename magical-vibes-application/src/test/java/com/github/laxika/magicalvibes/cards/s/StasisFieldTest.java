package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StasisField.class, AirElemental.class, FountainOfYouth.class, GrizzlyBears.class,
        ProdigalPyromancer.class})
class StasisFieldTest extends BaseCardTest {

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StasisField()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void setsBasePowerToughnessGrantsDefenderAndRemovesAbilities() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = new Permanent(new StasisField());
        aura.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void preventsEnchantedCreatureFromActivatingAbilities() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        Permanent aura = new Permanent(new StasisField());
        aura.setAttachedTo(pyromancer.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingAuraRestoresCreature() {
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = new Permanent(new StasisField());
        aura.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.DEFENDER)).isFalse();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new StasisField()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
