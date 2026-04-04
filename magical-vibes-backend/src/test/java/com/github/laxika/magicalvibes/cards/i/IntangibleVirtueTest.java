package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntangibleVirtueTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Intangible Virtue has correct static boost effect for tokens with vigilance")
    void hasCorrectStaticEffect() {
        IntangibleVirtue card = new IntangibleVirtue();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).containsExactly(Keyword.VIGILANCE);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentIsTokenPredicate.class);
    }

    // ===== Static effect: buffs own creature tokens =====

    @Test
    @DisplayName("Own creature tokens get +1/+1 and vigilance")
    void buffsOwnCreatureTokens() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        Permanent token = findPermanent(player1, "Soldier Token");

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-token creatures")
    void doesNotBuffNonTokenCreatures() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's creature tokens")
    void doesNotBuffOpponentTokens() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player2, createTokenCreature("Zombie Token", 2, 2));

        Permanent opponentToken = findPermanent(player2, "Zombie Token");

        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Intangible Virtues give +2/+2 and vigilance to tokens")
    void twoVirtuesStack() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, createTokenCreature("Spirit Token", 1, 1));

        Permanent token = findPermanent(player1, "Spirit Token");

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Intangible Virtue leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        Permanent token = findPermanent(player1, "Soldier Token");

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();

        // Remove Intangible Virtue
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Intangible Virtue"));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Intangible Virtue resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        Permanent token = findPermanent(player1, "Soldier Token");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();

        harness.setHand(player1, List.of(new IntangibleVirtue()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        Permanent token = findPermanent(player1, "Soldier Token");

        // Simulate a temporary spell boost
        token.setPowerModifier(token.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5); // 1 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        token.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2); // 1 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    // ===== Helper methods =====

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }

}
