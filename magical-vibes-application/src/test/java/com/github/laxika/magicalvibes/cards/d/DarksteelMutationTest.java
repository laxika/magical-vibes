package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelMutation.class, FountainOfYouth.class, Ornithopter.class, SerraAngel.class})
class DarksteelMutationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreatureIntoIndestructibleInsectArtifactCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castDarksteelMutation(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, angel)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveCardTypes(gd, angel))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castDarksteelMutation(angel);

        Permanent aura = findPermanent(player1, "Darksteel Mutation");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, angel)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveCardTypes(gd, angel)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castDarksteelMutation(Permanent target) {
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
