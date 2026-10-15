package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.m.ManedServal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolidFooting.class, FountainOfYouth.class, ManedServal.class})
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
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SolidFooting());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Flash allows enchanting an opponent's creature during their upkeep")
    void flashAllowsEnchantingOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new ManedServal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SolidFooting()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Solid Footing").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Losing and regaining vigilance changes combat damage without removing the boost")
    void vigilanceConditionUpdatesDynamically() {
        Permanent creature = addCreatureReady(player1, new ManedServal());
        attachAura(creature);
        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(5);

        creature.getRemovedKeywords().add(Keyword.VIGILANCE);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(2);

        creature.getRemovedKeywords().remove(Keyword.VIGILANCE);

        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("A vigilant attacker uses toughness even when its power is greater")
    void vigilantAttackerUsesLowerToughness() {
        Permanent creature = addCreatureReady(player1, new ManedServal());
        creature.setPowerModifier(5);
        attachAura(creature);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
