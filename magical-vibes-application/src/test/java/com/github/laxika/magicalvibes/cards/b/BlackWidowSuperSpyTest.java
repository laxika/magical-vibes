package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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
    @DisplayName("Declining the counter allows a normal-cost any-color cast in the main phase")
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

        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, nonland.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(nonland.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Exiling only lands still allows putting a counter on Black Widow")
    void onlyLandsStillAllowCounter() {
        Permanent widow = addAttackingBlackWidow();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(widow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty damaged player's library still allows putting a counter on Black Widow")
    void emptyLibraryStillAllowsCounter() {
        Permanent widow = addAttackingBlackWidow();
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(widow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the counter does not offer to cast a sorcery during combat")
    void sorceryCannotBeCastDuringTriggerResolution() {
        addAttackingBlackWidow();
        Card nonland = new Divination();
        harness.setLibrary(player2, List.of(nonland));
        harness.addMana(player1, ManaColor.GREEN, 3);

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(nonland.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The exile casting permission expires at the end of the turn")
    void unusedCastPermissionExpires() {
        addAttackingBlackWidow();
        Card nonland = new BlackWidowSuperSpy();
        harness.setLibrary(player2, List.of(nonland));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(nonland.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(nonland.getId());
    }

    @Test
    @DisplayName("A source that left the battlefield cannot choose a counter instead of casting permission")
    void absentSourceCannotChooseImpossibleCounter() {
        Permanent widow = addAttackingBlackWidow();
        Card nonland = new BlackWidowSuperSpy();
        harness.setLibrary(player2, List.of(nonland));

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(widow);
        harness.setGraveyard(player1, List.of(widow.getCard()));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(nonland.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(nonland.getId())).isEqualTo(player1.getId());
    }

    private Permanent addAttackingBlackWidow() {
        Permanent widow = addCreatureReady(player1, new BlackWidowSuperSpy());
        widow.setAttacking(true);
        return widow;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
