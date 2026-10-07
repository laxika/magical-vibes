package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GuardianBeast;
import com.github.laxika.magicalvibes.cards.t.TheEarthCrystal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StiltzkinMoogleMerchant.class, Forest.class, GuardianBeast.class, TheEarthCrystal.class})
class StiltzkinMoogleMerchantTest extends BaseCardTest {

    @Test
    @DisplayName("Transfers another permanent to an opponent and draws a card")
    void transfersPermanentAndDraws() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin),
                0,
                List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(stiltzkin.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot target Stiltzkin itself")
    void cannotTargetSourcePermanent() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin),
                0,
                List.of(player2.getId(), stiltzkin.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another permanent you control");
    }

    @Test
    void cannotGivePermanentToYourself() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin), 0,
                List.of(player1.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotGiveOpponentsPermanent() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin), 0,
                List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDrawWhenPermanentLeavesBeforeResolution() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin), 0,
                List.of(player2.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin), 0,
                List.of(player2.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(stiltzkin);
        gd.playerGraveyards.get(player1.getId()).add(stiltzkin.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void doesNotDrawWhenYouLoseControlOfTargetBeforeResolution() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin), 0,
                List.of(player2.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void gainsLifeFromCombatDamage() {
        addCreatureReady(player1, new StiltzkinMoogleMerchant());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    @CardUsed({GuardianBeast.class, TheEarthCrystal.class})
    void doesNotDrawWhenControlChangeIsPrevented() {
        Permanent stiltzkin = addCreatureReady(player1, new StiltzkinMoogleMerchant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheEarthCrystal());
        harness.addToBattlefield(player1, new GuardianBeast());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stiltzkin), 0,
                List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
