package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolidFooting.class, FountainOfYouth.class})
class SolidFootingTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreature(2, 3);
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("A vigilant enchanted creature assigns combat damage equal to its toughness")
    void vigilanceMakesCreatureAssignDamageEqualToToughness() {
        Permanent creature = addCreature(2, 3, Keyword.VIGILANCE);
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Aura's effects stop when it leaves the battlefield")
    void effectsStopWhenAuraLeavesBattlefield() {
        Permanent creature = addCreature(2, 3, Keyword.VIGILANCE);
        Permanent aura = attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Solid Footing can enchant only a creature")
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SolidFooting()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(int power, int toughness, Keyword... keywords) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        EnumSet<Keyword> keywordSet = EnumSet.noneOf(Keyword.class);
        keywordSet.addAll(List.of(keywords));
        card.setKeywords(keywordSet);
        return addCreatureReady(player1, card);
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new SolidFooting());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
