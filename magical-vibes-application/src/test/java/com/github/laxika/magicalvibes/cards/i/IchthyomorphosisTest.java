package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({Ichthyomorphosis.class, SerraAngel.class, FountainOfYouth.class})
class IchthyomorphosisTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature becomes a blue 0/1 Fish and loses its abilities")
    void transformsEnchantedCreature() {
        Permanent creature = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.FISH);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Removing Ichthyomorphosis restores the creature")
    void removingAuraRestoresCreature() {
        Permanent creature = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Ichthyomorphosis");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, creature)).contains(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ichthyomorphosis can target only a creature")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
