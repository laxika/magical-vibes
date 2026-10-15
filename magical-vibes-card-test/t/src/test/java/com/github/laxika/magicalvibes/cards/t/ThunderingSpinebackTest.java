package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MerfolkBranchwalker;
import com.github.laxika.magicalvibes.cards.r.RegisaurAlpha;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderingSpineback.class, RegisaurAlpha.class, MerfolkBranchwalker.class})
class ThunderingSpinebackTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dinosaur creatures you control get +1/+1")
    void buffsOtherDinosaursYouControl() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        Permanent dino = findPermanent(player1, "Regisaur Alpha");

        // Regisaur Alpha is 4/4 base + 1/1 from Thundering Spineback = 5/5
        assertThat(gqs.getEffectivePower(gd, dino)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dino)).isEqualTo(5);
    }

    @Test
    @DisplayName("Thundering Spineback does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ThunderingSpineback());

        Permanent spineback = findPermanent(player1, "Thundering Spineback");

        assertThat(gqs.getEffectivePower(gd, spineback)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spineback)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff non-Dinosaur creatures")
    void doesNotBuffNonDinosaurs() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player1, new MerfolkBranchwalker());

        Permanent merfolk = findPermanent(player1, "Merfolk Branchwalker");

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not buff opponent's Dinosaur creatures")
    void doesNotBuffOpponentDinosaurs() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player2, new RegisaurAlpha());

        Permanent opponentDino = findPermanent(player2, "Regisaur Alpha");

        assertThat(gqs.getEffectivePower(gd, opponentDino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentDino)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus is removed when Thundering Spineback leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        Permanent dino = findPermanent(player1, "Regisaur Alpha");
        assertThat(gqs.getEffectivePower(gd, dino)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Thundering Spineback"));

        assertThat(gqs.getEffectivePower(gd, dino)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dino)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated ability creates a 3/3 green Dinosaur token with trample")
    void activatedAbilityCreatesToken() {
        harness.addToBattlefield(player1, new ThunderingSpineback());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Token should be on battlefield (plus Thundering Spineback itself)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        Permanent token = findPermanent(player1, "Dinosaur");

        // Token is 3/3 base + 1/1 from Thundering Spineback lord = 4/4
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Activated ability can be used multiple times per turn")
    void activatedAbilityCanBeUsedMultipleTimes() {
        harness.addToBattlefield(player1, new ThunderingSpineback());

        // First activation
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Second activation
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = countPermanents(player1, "Dinosaur");
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability does not require tap")
    void activatedAbilityDoesNotRequireTap() {
        Permanent spineback = harness.addToBattlefieldAndReturn(player1, new ThunderingSpineback());
        spineback.tap();
        spineback.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(1);
        assertThat(spineback.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two Spinebacks boost each other and their token receives both bonuses")
    void multipleSpinebacksStackBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThunderingSpineback());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThunderingSpineback());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        Permanent token = findPermanent(player1, "Dinosaur");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
    }

    @Test
    @DisplayName("Token ability resolves after its source leaves, without the lord bonus")
    void abilityResolvesAfterSourceLeaves() {
        Permanent spineback = harness.addToBattlefieldAndReturn(player1, new ThunderingSpineback());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(spineback);
        gd.playerGraveyards.get(player1.getId()).add(spineback.getCard());
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dinosaur");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        assertThat(countPermanents(player2, "Dinosaur")).isZero();
    }

}
