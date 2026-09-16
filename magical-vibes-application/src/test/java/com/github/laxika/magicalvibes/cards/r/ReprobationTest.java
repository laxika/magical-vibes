package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FountainOfYouth.class, Reprobation.class, SerraAngel.class})
class ReprobationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = new Permanent(new Reprobation());
        aura.setAttachedTo(angel.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardTypes(gd, angel)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.COWARD);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = new Permanent(new Reprobation());
        aura.setAttachedTo(angel.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Reprobation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
