package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackWidowSuperSpy.class, Divination.class, Forest.class})
class BlackWidowSuperSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles through lands and putting a counter leaves the hit exiled")
    void combatDamageCanPutCounterOnBlackWidow() {
        Permanent widow = addAttackingBlackWidow();
        Card land = new Forest();
        Card nonland = new Divination();
        harness.setLibrary(player2, List.of(land, nonland));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(widow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(land.getId(), nonland.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(nonland.getId());
    }

    @Test
    @DisplayName("Declining the counter offers a normal-cost any-color cast until end of turn")
    void decliningCounterOffersNormalCostAnyColorCast() {
        addAttackingBlackWidow();
        Card land = new Forest();
        Card nonland = new Divination();
        harness.setLibrary(player2, List.of(land, nonland));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exilePlayPermissions.get(nonland.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(nonland.getId());
        assertThat(gd.exilePlayAnyManaType).contains(nonland.getId());

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(nonland.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Exiling only lands does not offer either choice")
    void onlyLandsDoNotOfferChoice() {
        Permanent widow = addAttackingBlackWidow();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();

        assertThat(widow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addAttackingBlackWidow() {
        Permanent widow = addCreatureReady(player1, new BlackWidowSuperSpy());
        widow.setAttacking(true);
        return widow;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
