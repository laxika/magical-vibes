package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.m.MeteorGolem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitnessProtection.class, SerraAngel.class, FountainOfYouth.class, MeteorGolem.class, ShivanDragon.class})
class WitnessProtectionTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        aura.setAttachedTo(angel.getId());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, angel);

        assertThat(gqs.getEffectiveName(gd, angel)).isEqualTo("Legitimate Businessperson");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.hasColor(gd, angel, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.isCreature(gd, angel)).isTrue();
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.CITIZEN);
        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.cardTypeOverriding()).isTrue();
    }

    @Test
    void removingAuraRestoresNameAndCharacteristics() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        aura.setAttachedTo(angel.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveName(gd, angel)).isEqualTo("Serra Angel");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
    }

    @Test
    void cannotEnchantNoncreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, java.util.List.of(new WitnessProtection()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void castingAuraReplacesCreatureTypesAndRemovesBothKeywords() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, java.util.List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Witness Protection").getAttachedTo()).isEqualTo(angel.getId());
        assertThat(gqs.getEffectiveName(gd, angel)).isEqualTo("Legitimate Businessperson");
        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.CITIZEN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, angel, CardSubtype.ANGEL)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void removesArtifactTypeAndPreservesPowerToughnessCounters() {
        Permanent golem = harness.addToBattlefieldAndReturn(player2, new MeteorGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        aura.setAttachedTo(golem.getId());

        assertThat(gqs.getEffectiveCardTypes(gd, golem)).containsExactly(CardType.CREATURE);
        assertThat(gqs.hasEffectiveSubtype(gd, golem, CardSubtype.GOLEM)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, golem, CardSubtype.CITIZEN)).isTrue();
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isArtifact(gd, golem)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, golem, CardSubtype.GOLEM)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, golem, CardSubtype.CITIZEN)).isFalse();
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);
    }

    @Test
    void replacesOriginalColorAndPreventsActivatedAbility() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WitnessProtection());
        aura.setAttachedTo(dragon.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gqs.hasColor(gd, dragon, CardColor.RED)).isFalse();
        assertThat(gqs.hasColor(gd, dragon, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, dragon, CardColor.WHITE)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasColor(gd, dragon, CardColor.RED)).isTrue();
        assertThat(gqs.hasColor(gd, dragon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasColor(gd, dragon, CardColor.WHITE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
    }
}
