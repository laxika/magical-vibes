package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntangibleVirtue.class, WalkingCorpse.class, Opalescence.class})
class IntangibleVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Own creature tokens get +1/+1 and vigilance")
    void buffsOwnCreatureTokens() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Soldier Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-token creatures")
    void doesNotBuffNonTokenCreatures() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, new WalkingCorpse());

        Permanent bears = findPermanent(player1, "Walking Corpse");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's creature tokens")
    void doesNotBuffOpponentTokens() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2,
                createTokenCreature("Zombie Token", 2, 2));

        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Two Intangible Virtues give +2/+2 and vigilance to tokens")
    void twoVirtuesStack() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        harness.addToBattlefield(player1, new IntangibleVirtue());
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Spirit Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Bonus is removed when Intangible Virtue leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Soldier Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();

        // Remove Intangible Virtue
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Intangible Virtue"));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Intangible Virtue resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Soldier Token", 1, 1));
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();

        harness.castFromHand(player1, new IntangibleVirtue(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        Permanent token = harness.addToBattlefieldAndReturn(player1,
                createTokenCreature("Soldier Token", 1, 1));

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

    @Test
    @DisplayName("A token copy animated by Opalescence benefits from its own ability")
    void animatedTokenCopyBuffsItself() {
        Card tokenCopy = new IntangibleVirtue().createRuntimeCopyWithNewId();
        tokenCopy.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCopy);

        assertThat(gqs.isCreature(gd, token)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();

        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The bonus follows a creature token's current controller")
    void bonusFollowsTokenController() {
        harness.addToBattlefield(player1, new IntangibleVirtue());
        Permanent token = harness.addToBattlefieldAndReturn(player2,
                createTokenCreature("Spirit Token", 1, 1));

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        gd.playerBattlefields.get(player2.getId()).remove(token);
        gd.playerBattlefields.get(player1.getId()).add(token);

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

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
