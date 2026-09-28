package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
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

@CardUsed({AlmostPerfect.class, FountainOfYouth.class, SerraAngel.class})
class AlmostPerfectTest extends BaseCardTest {

    @Test
    @DisplayName("Almost Perfect sets the enchanted creature's base power and toughness to 9/10 and grants indestructible")
    void setsBasePowerToughnessAndGrantsIndestructible() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castAndResolve(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Removing Almost Perfect restores the creature's original characteristics")
    void removingAuraRestoresCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castAndResolve(angel);

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AlmostPerfect)
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Almost Perfect can target only a creature")
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new AlmostPerfect()));
        addManaForAlmostPerfect();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new AlmostPerfect()));
        addManaForAlmostPerfect();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addManaForAlmostPerfect() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
