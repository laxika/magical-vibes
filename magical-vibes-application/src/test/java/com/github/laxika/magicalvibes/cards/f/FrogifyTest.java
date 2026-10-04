package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({Frogify.class, FountainOfYouth.class, Gingerbrute.class, Ornithopter.class, SerraAngel.class})
class FrogifyTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreature() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castFrogify(thopter);

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, thopter)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveCardTypes(gd, thopter)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, thopter)).containsExactly(CardSubtype.FROG);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castFrogify(angel);

        var frogifyId = harness.getPermanentId(player1, "Frogify");
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(frogifyId));

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, angel)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectiveCardTypes(gd, angel)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void plusOneCountersApplyOnTopOfNewBaseStats() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        thopter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castFrogify(thopter);

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
        assertThat(thopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void minusOneCounterMakesEnchantedCreatureDieAndAuraGoToGraveyard() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        angel.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        castFrogify(angel);

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player1, "Frogify");
        harness.assertInGraveyard(player1, "Frogify");
    }

    @Test
    void removesActivatedAbilitiesFromOwnCreature() {
        Permanent gingerbrute = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        castFrogify(gingerbrute);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.hasKeyword(gd, gingerbrute, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectiveCardTypes(gd, gingerbrute)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, gingerbrute)).containsExactly(CardSubtype.FROG);
        assertThat(gqs.hasEffectiveSubtype(gd, gingerbrute, CardSubtype.FOOD)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Gingerbrute");
        harness.assertLife(player1, 20);
    }

    private void castFrogify(Permanent target) {
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
