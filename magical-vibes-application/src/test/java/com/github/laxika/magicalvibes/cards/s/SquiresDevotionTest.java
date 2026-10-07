package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquiresDevotion.class, GrizzlyBears.class, FountainOfYouth.class, Naturalize.class})
class SquiresDevotionTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches to a creature, boosts it, and creates a lifelink Vampire token")
    void attachesBoostsAndCreatesToken() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SquiresDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.VAMPIRE))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.setHand(player1, List.of(new SquiresDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
    @Test
    @DisplayName("Enchanting an opposing creature creates the token for the Aura controller")
    void enchantsOpposingCreatureAndCreatesTokenForAuraController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquiresDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger creates its token after the Aura is destroyed")
    void enterTriggerSurvivesAuraRemoval() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquiresDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).isEmpty();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Squire's Devotion"));

        harness.assertInGraveyard(player1, "Squire's Devotion");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Both the enchanted creature and Vampire token gain life from combat damage")
    void enchantedCreatureAndTokenGainLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquiresDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        for (Permanent attacker : List.of(bears, token)) {
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            attacker.setAttackTarget(player2.getId());
        }

        harness.resolveCombatDamage();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
