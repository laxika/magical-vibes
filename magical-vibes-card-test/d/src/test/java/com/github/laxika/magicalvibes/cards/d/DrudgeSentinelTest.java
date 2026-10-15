package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrudgeSentinel.class, Eviscerate.class, ShivanFire.class})
class DrudgeSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps and grants indestructible")
    void activatingAbilityTapsAndGrantsIndestructible() {
        Permanent sentinel = addCreatureReady(player1, new DrudgeSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Can activate ability when already tapped — still gains indestructible")
    void canActivateWhenAlreadyTapped() {
        Permanent sentinel = addCreatureReady(player1, new DrudgeSentinel());
        sentinel.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Casting from hand, then activating ability works")
    void castThenActivate() {
        harness.setHand(player1, List.of(new DrudgeSentinel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Drudge Sentinel");
        assertThat(sentinel).isNotNull();

        // Remove summoning sickness and add mana for ability
        sentinel.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Summoning sickness permits activation; tapping and protection wait for resolution")
    void canActivateWhileSummoningSick() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new DrudgeSentinel());
        sentinel.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sentinel.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleExpires() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new DrudgeSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Two mana cannot pay the activation cost")
    void requiresThreeMana() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new DrudgeSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(sentinel.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The ability protects against a destroy spell on the stack")
    void survivesDestroySpell() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new DrudgeSentinel());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, sentinel.getId());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sentinel);
        harness.assertInGraveyard(player1, "Eviscerate");
    }

    @Test
    @DisplayName("The ability protects against lethal damage on the stack")
    void survivesLethalDamage() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new DrudgeSentinel());
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, sentinel.getId());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sentinel);
        harness.assertInGraveyard(player1, "Shivan Fire");
    }

}
