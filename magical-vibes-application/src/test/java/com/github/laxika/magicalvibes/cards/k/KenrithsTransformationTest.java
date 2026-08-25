package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({KenrithsTransformation.class, FountainOfYouth.class, Ornithopter.class})
class KenrithsTransformationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreatureIntoGreenElk() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castAndResolve(thopter);

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, thopter)).containsExactly(CardColor.GREEN);
        assertThat(gqs.getEffectiveCardTypes(gd, thopter)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, thopter)).containsExactly(CardSubtype.ELK);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
    }

    @Test
    void drawsACardWhenItEnters() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLibrary(player1, List.of(new FountainOfYouth()));

        castAndResolve(thopter);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castAndResolve(thopter);

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KenrithsTransformation)
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, thopter)).isEmpty();
        assertThat(gqs.getEffectiveCardTypes(gd, thopter))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, thopter)).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new KenrithsTransformation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new KenrithsTransformation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
