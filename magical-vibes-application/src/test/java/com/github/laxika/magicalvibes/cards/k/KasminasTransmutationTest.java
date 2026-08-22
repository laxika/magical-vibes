package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({KasminasTransmutation.class, AirElemental.class, ProdigalPyromancer.class, FountainOfYouth.class})
class KasminasTransmutationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has base power and toughness 1/1 and loses all abilities")
    void transformsEnchantedCreature() {
        Permanent airElemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        Permanent aura = new Permanent(new KasminasTransmutation());
        aura.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Removing Kasmina's Transmutation restores the enchanted creature")
    void removalRestoresCreature() {
        Permanent airElemental = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        Permanent aura = new Permanent(new KasminasTransmutation());
        aura.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, airElemental)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, airElemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("An enchanted creature cannot activate its abilities")
    void removesActivatedAbilities() {
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        Permanent aura = new Permanent(new KasminasTransmutation());
        aura.setAttachedTo(pyromancer.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kasmina's Transmutation can target only a creature")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new KasminasTransmutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
