package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LyraDawnbringer.class, SerraAngel.class, CabalEvangel.class})
class LyraDawnbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Angel creatures you control get +1/+1 and lifelink")
    void buffsOtherAngelsYouControl() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        // Serra Angel is 4/4 base + 1/1 from Lyra = 5/5
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Lyra Dawnbringer does not buff itself")
    void doesNotBuffItself() {
        Permanent lyra = harness.addToBattlefieldAndReturn(player1, new LyraDawnbringer());

        // Lyra is 5/5 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, lyra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lyra)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff non-Angel creatures")
    void doesNotBuffNonAngels() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());

        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, evangel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Angel creatures")
    void doesNotBuffOpponentAngels() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        Permanent opponentAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        assertThat(gqs.getEffectivePower(gd, opponentAngel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentAngel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opponentAngel, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Two Lyra Dawnbringers buff each other")
    void twoLyrasBuffEachOther() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new LyraDawnbringer());

        List<Permanent> lyras = findPermanents(player1, "Lyra Dawnbringer");

        assertThat(lyras).hasSize(2);
        for (Permanent lyra : lyras) {
            assertThat(gqs.getEffectivePower(gd, lyra)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, lyra)).isEqualTo(6);
            assertThat(gqs.hasKeyword(gd, lyra, Keyword.LIFELINK)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Lyras give +2/+2 and lifelink to other Angels")
    void twoLyrasStackBonuses() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new LyraDawnbringer());
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        // 4/4 base + 2/2 from two Lyras = 6/6
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Bonus is removed when Lyra Dawnbringer leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lyra Dawnbringer"));

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Lyra Dawnbringer resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isFalse();

        harness.castFromHand(player1, new LyraDawnbringer(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        serra.setPowerModifier(serra.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(10); // 4 base + 5 spell + 1 static

        serra.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5); // 4 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("An Angel gains life equal to the damage it deals with Lyra's granted lifelink")
    void grantedLifelinkGainsLifeInCombat() {
        harness.addToBattlefield(player1, new LyraDawnbringer());
        addCreatureReady(player1, new SerraAngel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Marked damage becomes lethal when Lyra's toughness bonus ends")
    void losingBonusMakesMarkedDamageLethal() {
        Permanent lyra = harness.addToBattlefieldAndReturn(player1, new LyraDawnbringer());
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        serra.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Serra Angel");

        gd.playerBattlefields.get(player1.getId()).remove(lyra);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Choosing one Lyra for the legend rule removes the extra Angel bonus")
    void legendChoiceRemovesExtraBonus() {
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new LyraDawnbringer());
        harness.addToBattlefield(player1, new LyraDawnbringer());
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.runStateBasedActions();
        harness.handlePermanentChosen(player1, kept.getId());

        assertThat(countPermanents(player1, "Lyra Dawnbringer")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, kept)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, kept)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serra, Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "Lyra Dawnbringer");
    }

}
